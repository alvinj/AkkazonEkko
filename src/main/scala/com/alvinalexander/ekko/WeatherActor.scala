package com.alvinalexander.ekko

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.xml.XML

class WeatherActor extends Actor with ActorLogging {

    val weatherUrl = "http://weather.yahooapis.com/forecastrss?p=%s&u=f"
    val zipCode = "80301"

    val brain = context.actorSelection("/user/Brain")

    val regexesWeRespondTo = List(
        ".*weather.*",
        "forecast",
        "weather forecast"
    )
    
    val letUserKnowPhrasesWeRespondTo = List(
        "weather",
        "forecast",
        "current weather",
        "weather forecast"
    )

    def receive = {

        case WillYouHandlePhrase(phrase) =>
            val weWillHandle = Utils.matchesARegex(regexesWeRespondTo, phrase)
            log.info(s"WeatherActor: weWillHandle = $weWillHandle")
            sender ! weWillHandle
            if (weWillHandle) {
                log.info(s"WeatherActor will handle '$phrase'")
                if (phrase.matches(".*forecast.*")) {
                    tellUserTheForecast(getWeatherForecast)
                } else {
                    tellUserTheForecast(getCurrentWeather)
                }
            }

        case WhatPhrasesCanYouHandle =>
            sender ! letUserKnowPhrasesWeRespondTo

        case unknown =>
            log.info("WeatherActor got an unknown message: " + unknown)

    }
    
    private def tellUserTheForecast(f:() => String) {
        brain ! SpeakText("stand by ...")
        val w = f()
        brain ! SpeakText(w)
    }

    def getWeatherForecast(): String = {
        try {
            // 1) get the content from the yahoo weather api url (https://developer.yahoo.com/weather/)
            val content = getContent("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22boulder%2C%20co%22)&format=xml&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")

            // 2) convert it to xml
            val xml = XML.loadString(content)

            // 3) get the elements i want
            val days = Map("Mon" -> "Monday",
                "Tue" -> "Tuesday",
                "Wed" -> "Wednesday",
                "Thu" -> "Thursday",
                "Fri" -> "Friday",
                "Sat" -> "Saturday",
                "Sun" -> "Sunday")
            val sb = new StringBuilder
            for (i <- 0 until 2) {
                val day = (xml \\ "channel" \\ "item" \ "forecast")(i) \ "@day"
                val date = (xml \\ "channel" \\ "item" \ "forecast")(i) \ "@date"
                val low = (xml \\ "channel" \\ "item" \ "forecast")(i) \ "@low"
                val high = (xml \\ "channel" \\ "item" \ "forecast")(i) \ "@high"
                val text = (xml \\ "channel" \\ "item" \ "forecast")(i) \ "@text"
                if (i == 0) {
                    sb.append(String.format("Here's the forecast.\nFor %s, a low of %s, a high of %s, and %s skies. ", days(day.toString.trim), low, high, text))
                } else {
                    sb.append(String.format("\nFor %s, a low of %s, a high of %s, and %s skies.\n", days(day.toString.trim), low, high, text))
                }
            }
            return sb.toString

        } catch {
            case e: Exception =>
                log.error(s"WeatherActor got this exception: ${e.getMessage}")
                "Sorry, could not get the weather."
            case unknown: Throwable => 
                log.error(s"WeatherActor got this exception: $unknown")
                "Sorry, could not get the weather."
        }
    }

    def getCurrentWeather(): String = {
        try {
            // (1) get the content from the yahoo weather api url (https://developer.yahoo.com/weather/)
            val content = getContent("https://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20%3D%2012793014&format=xml&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")

            // (2) convert it to xml
            val xml = XML.loadString(content)
            assert(xml.isInstanceOf[scala.xml.Elem]) // needed?

            // (3) search the xml for the nodes i want
            val temp = (xml \\ "channel" \\ "item" \ "condition" \ "@temp") text
            val text = (xml \\ "channel" \\ "item" \ "condition" \ "@text") text

            // (4) print the results
            return String.format("The current temperature is %s degrees, and the sky is %s.", temp, text.toLowerCase)
        } catch {
            case e: Exception =>
                log.error(s"WeatherActor got this exception: ${e.getMessage}")
                "Sorry, could not get the weather."
            case unknown: Throwable => 
                log.error(s"WeatherActor got this exception: $unknown")
                "Sorry, could not get the weather."
        }
    }

    def buildWeatherUrl(weatherUrl: String, zipCode: String): String = String.format(weatherUrl, zipCode)

    @throws(classOf[java.io.IOException])
    @throws(classOf[java.net.SocketTimeoutException])
    def getContent(url: String,
                   connectTimeout: Int = 5000,
                   readTimeout: Int = 5000,
                   requestMethod: String = "GET") = {
        import java.net.{ URL, HttpURLConnection }
        val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
        connection.setConnectTimeout(connectTimeout)
        connection.setReadTimeout(readTimeout)
        connection.setRequestMethod(requestMethod)
        val inputStream = connection.getInputStream
        val content = io.Source.fromInputStream(inputStream).mkString
        if (inputStream != null) inputStream.close
        content
    }

}



