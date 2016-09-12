package com.alvinalexander.ekko

import akka.actor.Actor
import akka.event.Logging
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorLogging

class Brain extends Actor with ActorLogging {

    // actors we communicate with
    val mouth = context.actorSelection("/user/Mouth")

    // a child of the Brain
    val brainMessageProcessor = context.actorOf(Props[BrainMessageProcessor])

    // handle messages sent to us
    def receive = {

        case MessageFromEars(msg) =>
            log.info(s"Brain got a MessageFromEars ($msg)")
            brainMessageProcessor ! MessageFromEars(msg)

        case SpeakText(msg) =>
            log.info(s"Brain got a SpeakText message ($msg)")
            mouth ! SpeakText(msg)
            
        case SpeakList(list) =>
            log.info(s"Brain got a SpeakList message")
            mouth ! SpeakList(list)

//        case ShowPrompt =>
//            mouth ! ShowPrompt

        case unknown =>
            log.info("Brain got an unknown message: " + unknown)

    }    

}

