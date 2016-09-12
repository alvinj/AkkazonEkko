package com.alvinalexander.ekko.workers.tribune_sports

import akka.actor.Actor
import akka.actor.ActorLogging
import com.alvinalexander.ekko._
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala
import scala.xml.XML

class TribuneSportsActor extends Actor with ActorLogging {

    val brain = context.actorSelection("/user/Brain")
    val className = this.getClass.getSimpleName

    val regexesWeRespondTo = List(
        "trib",
        "tribune",
        "trib.* sports"
    )

    def receive = {

        case WillYouHandlePhrase(s) =>
            val weWillHandle = Utils.matchesARegex(regexesWeRespondTo, s)
            sender ! weWillHandle
            if (weWillHandle) {
                //TODO use `${this.getClass.getSimpleName}` (or similar) in other classes
                log.info(s"$className will handle '$s'")
                //brain ! SpeakText(TribuneUtils.getTribuneHeadlines)
                brain ! SpeakText(getTribuneSports)
            }

        case WhatPhrasesCanYouHandle =>
            sender ! regexesWeRespondTo

        case unknown =>
            log.info("$className got an unknown message: " + unknown)

    }
    
    //TODO this needs a lot of work
    //TODO this code can also be generalized for any XML/RSS resource
    private def getTribuneSports: String = {
        val topicsOfInterest = List("Bulls", "Cubs", "Colts", "Bears",
            "Chris Sale", "Rizzo", "Bryant", "Arrieta", "Hendricks", "Lester")
        val xml = XML.load("http://www.chicagotribune.com/sports/rss2.0.xml")
        val titleNodes = (xml \\ "item" \ "title")
        val headlines = for {
            t <- titleNodes
            if Utils.listContainsASubstringOfPhrase(topicsOfInterest, t.text)
        } yield t.text
        headlines.mkString("\n")
    }

}




