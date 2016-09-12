package com.alvinalexander.ekko
  
import akka.actor.Actor
import akka.event.Logging
import akka.actor.ActorLogging

class GoodbyeActor extends Actor with ActorLogging {

    val brain = context.actorSelection("/user/Brain")

    val goodbyeRegexesWeRespondTo = List(
        "adios",
        "aloha",
        "bye",
        "exit",
        "goodbye",
        "hasta.*",
        "quit"
    )

    def receive = {
        
        case WillYouHandlePhrase(s) =>
            val weWillHandle = Utils.matchesARegex(goodbyeRegexesWeRespondTo, s)
            sender ! weWillHandle
            if (weWillHandle) {
                log.info(s"GoodbyeActor will handle '$s'")
                handleGoodbye
            }

        case WhatPhrasesCanYouHandle =>
            sender ! goodbyeRegexesWeRespondTo
            
        case unknown =>
            log.info("BrainMessageProcessor got an unknown message: " + unknown)

    }
    
    private def handleGoodbye {
        brain ! SpeakText(getRandomGoodbyePhrase)
        Thread.sleep(500)
        // TODO a better way to do this?
        context.system.terminate
        System.exit(0)
    }

    // TODO (DRY - repeated in HelloActor)
    private def isAPhraseWeUnderstand(phrase: String): Boolean = {
        for (s <- goodbyeRegexesWeRespondTo) {
            if (phrase.matches(s)) return true;
        }
        false
    }

    private def getRandomGoodbyePhrase: String = {
        val phrases = List(
            "Aloha",
            "Bye!",
            "Goodbye.",
            "Hasta luego.",
            "Arrivederci")
        Utils.getRandomElement(phrases)
    }

}




