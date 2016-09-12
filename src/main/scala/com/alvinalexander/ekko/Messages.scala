package com.alvinalexander.ekko

/**
 * this file is a container for all of the messages that can 
 * be sent in the application
 */
  
case object Hello
case object SayHello
case object ShowPrompt
case class MessageFromEars(s: String)

case class SpeakText(text: String)
case class SpeakList(list: Seq[String])

// messages about whether actors can handle a phrase or not
case class WillYouHandlePhrase(s: String)

case object WhatPhrasesCanYouHandle
case class PhrasesICanHandle(xs: Seq[String])
