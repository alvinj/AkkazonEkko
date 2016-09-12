package com.alvinalexander.ekko

import akka.actor.Actor
import akka.event.Logging
import akka.actor.ActorLogging

class HelloActor extends Actor with ActorLogging {

    val mouth = context.actorSelection("/user/Mouth")
    
    val helloRegexesWeRespondTo = List(
        "hello",
        "hi",
        "hola",
        "yo"
    )

    def receive = {

        case WillYouHandlePhrase(s) =>
            val weWillHandle = Utils.matchesARegex(helloRegexesWeRespondTo, s)
            sender ! weWillHandle
            if (weWillHandle) {
                log.info(s"HelloActor will handle '$s'")
                mouth ! SpeakText(getRandomHelloPhrase)
            }

        case WhatPhrasesCanYouHandle =>
            sender ! helloRegexesWeRespondTo

        case unknown =>
            log.info("HelloActor got an unknown message: " + unknown)

    }

    private def getRandomHelloPhrase: String = {
        val phrases = List(
            "Good day",
            "Hello!",
            "How are you?",
            "Buenos dias",
            "It's a good day to be alive.",
            "Yo")
        Utils.getRandomElement(phrases)
    }

}




