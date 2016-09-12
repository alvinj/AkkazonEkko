package com.alvinalexander.ekko

import akka.actor.Actor
import akka.event.Logging
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.util.Timeout
import scala.concurrent.Await
import akka.pattern.ask              // need this for `?` ("ask")
import scala.concurrent.duration._   // need for things like `2 seconds`
import com.alvinalexander.ekko.workers.tribune_sports.TribuneSportsActor

/**
 * The Brain hands off messages to us that it receives from the Ears.
 * Our job is to process those messages.
 */
class BrainMessageProcessor extends Actor with ActorLogging {

    //val brain = context.parent
    val mouth = context.actorSelection("/user/Mouth")

    //TODO better regex's, ignore case
    val whatCanISayPhrases = List(
        "w",
        "w\\?",
        "what",
        "what\\?",
        "whatsay.*",
        "what can i say.*",
        "say what.*"
    )

    // TODO should i have this responsibility, or get this from elsewhere, like the brain?
    val goodbyeActor  = context.actorSelection("/user/GoodbyeActor")
    val helloActor    = context.actorSelection("/user/HelloActor")
    val toDoListActor = context.actorSelection("/user/ToDoListActor")
    val tribuneSportsActor = context.actorSelection("/user/TribuneSportsActor")
    val weatherActor  = context.actorSelection("/user/WeatherActor")
    val workers = List(goodbyeActor, helloActor, toDoListActor, tribuneSportsActor, weatherActor)

    def receive = {
        case MessageFromEars(msg) =>
            processMessageFromEars(msg)
        case unknown =>
            log.info("BrainMessageProcessor got an unknown message: " + unknown)
    }

    private def processMessageFromEars(msg: String) {
        if (Utils.matchesARegex(whatCanISayPhrases, msg)) {
            handleWhatCanISayRequest
        } else {
            if (msg.trim != "") {
                val handled = tryLettingActorsHandleThePhrase(msg)
                if (!handled) {
                    mouth ! SpeakText(s"could not handle the phrase '$msg'")
                }
            }
        }
    }

    /**
     * The user can ask, “What can I say?,” and this method
     * works to respond to that request.
     */
    private def handleWhatCanISayRequest: Unit = {
        // ask each actor what phrases they respond to
        // create a list from their replies
        implicit val timeout = Timeout(2 seconds)
        val listOfListOfPhrases = for (w <- workers) yield {
            val future = w ? WhatPhrasesCanYouHandle
            //TODO how to handle PhrasesICanHandle?
            val phrases = Await.result(future, timeout.duration).asInstanceOf[Seq[String]]
            phrases
        }
        val phrases = listOfListOfPhrases.flatten
        mouth ! SpeakList(phrases)
    }

    /**
     * Return true if an actor handled the phrase, false otherwise.
     * TODO this could hang up or be a little slow, hand it off to a child?
     */
    private def tryLettingActorsHandleThePhrase(msg: String): Boolean = {
        implicit val timeout = Timeout(2 seconds)
        for (w <- workers) {
            val future = w ? WillYouHandlePhrase(msg)
            val handled = Await.result(future, timeout.duration).asInstanceOf[Boolean]
            if (handled) {
                return true
            }
        }
        false
    }

}





