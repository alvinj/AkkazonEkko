package com.alvinalexander.ekko

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import java.util.Scanner
import com.alvinalexander.ekko.workers.tribune_sports.TribuneSportsActor

object Ekko4 extends App {

    // an actor needs an ActorSystem
    val system = ActorSystem("AlekaSystem")

    // create the actors in our play
    // TODO do this here, in the Brain, or elsewhere?
    val goodbyeActor       = system.actorOf(Props[GoodbyeActor],       name = "GoodbyeActor")
    val helloActor         = system.actorOf(Props[HelloActor],         name = "HelloActor")
    val toDoListActor      = system.actorOf(Props[ToDoListActor],      name = "ToDoListActor")
    val tribuneSportsActor = system.actorOf(Props[TribuneSportsActor], name = "TribuneSportsActor")
    val weatherActor       = system.actorOf(Props[WeatherActor],       name = "WeatherActor")

    val ears  = system.actorOf(Props[Ears],  name = "Ears")
    val mouth = system.actorOf(Props[Mouth], name = "Mouth")
    val brain = system.actorOf(Props[Brain], name = "Brain")

    // permanent loop to listen to user input
    val scanner = new Scanner(System.in)
    while (true) {
        Utils.showPrompt
        val input = scanner.nextLine
        ears ! input
    }
    

}