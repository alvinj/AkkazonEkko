package com.alvinalexander.ekko.workers.tribune_sports

import scala.collection.mutable.StringBuilder
import org.htmlcleaner.HtmlCleaner
import java.net.URL
import scala.collection.mutable.ListBuffer
import org.apache.commons.lang3.StringEscapeUtils

/**
 * TODO this code contains the old approach to "screen scraping" results off
 *      the tribune sports page. the algorithm can be made more general so
 *      you can screen-scrape any web page.
 */
object TribuneUtils {

    // TODO move this url to the properties file
    val TRIB_URL = "http://www.chicagotribune.com/sports/"

    def getTribuneHeadlines: String = {
        try {
            val url = TRIB_URL
            val stories = getHeadlinesFromUrlNew(url)
            val articles = getUniqueArticles(stories)
            val sentence = buildSentenceFromHeadlines(articles)
            sentence
        } catch {
            //TODO replace with logging
            case e: Exception =>
                System.err.println("(TribuneHeadlines) Caught a plain Exception: " + e.getMessage)
                return "Sorry, I had a problem checking the headlines. Error 1."
            case unknown: Throwable =>
                System.err.println("(EmailClient) Caught an unknown problem/exception.")
                return "Sorry, I had a problem checking the headlines. Error 2."
        }
    }

    private def getHeadlinesFromUrlNew(url: String): List[String] = {
        var stories = new ListBuffer[String]
        val cleaner = new HtmlCleaner
        val props = cleaner.getProperties
        val rootNode = cleaner.clean(new URL(url))
        val elements = rootNode.getElementsByName("h2", true)
        for (elem <- elements) {
            val classType = elem.getAttributeByName("class")
            if (classType != null && classType.contains("headline")) {
                val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
                stories += text
            }
        }

        // stories might be "dirty" with text like "&#039;", clean it up
        return stories.filter(storyContainsDesiredPhrase(_)).toList
    }

    private def getUniqueArticles(articles: List[String]): Set[String] = {
        return articles.toSet
    }

    private def buildSentenceFromHeadlines(headlines: Set[String]): String = {
        var sb = new StringBuilder
        for (h <- headlines) {
            sb.append(h + ". ")
        }
        sb.toString
    }

    // TODO move this list to the properties file
    private def storyContainsDesiredPhrase(s: String): Boolean = {
        val topics = List("Bulls", "Cubs", "Colts", "Bears", "Rose",
            "Noah", "Thibodeau", "Chris Sale", "Konerko",
            "Renteria", "Castro", "Rizzo", "Olt", "Samard", "Arrieta", "Hammel")
        for (t <- topics) {
            if (s.contains(t)) return true
        }
        false
    }

}

