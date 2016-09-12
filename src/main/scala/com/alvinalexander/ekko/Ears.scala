package com.alvinalexander.ekko

import akka.actor.Actor
import akka.event.Logging
import akka.actor.ActorLogging

class Ears extends Actor with ActorLogging {

    val brain = context.actorSelection("/user/Brain")

    def receive = {

        case s: String =>
            log.info("Ears received a string message: " + s)
            brain ! MessageFromEars(s)

        case unknown =>
            log.info("Ears got an unknown message: " + unknown)

    }

}


