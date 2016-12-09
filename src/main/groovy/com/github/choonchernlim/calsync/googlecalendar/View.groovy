package com.github.choonchernlim.calsync.googlecalendar

import com.google.api.services.calendar.model.*

class View {

    static void header(String name) {
        println()
        println("============== " + name + " ==============")
        println()
    }

    static void display(CalendarList feed) {
        if (feed.getItems() != null) {
            for (CalendarListEntry entry : feed.getItems()) {
                println()
                println("-----------------------------------------------")
                display(entry)
            }
        }
    }

    static void display(Events feed) {
        if (feed.getItems() != null) {
            for (Event entry : feed.getItems()) {
                println()
                println("-----------------------------------------------")
                display(entry)
            }
        }
    }

    static void display(CalendarListEntry entry) {
        println("ID      : " + entry.getId())
        println("Summary : " + entry.getSummary())
        if (entry.getDescription() != null) {
            println("Description: " + entry.getDescription())
        }
    }

    static void display(Calendar entry) {
        println("ID      : " + entry.getId())
        println("Summary : " + entry.getSummary())
        if (entry.getDescription() != null) {
            println("Description: " + entry.getDescription())
        }
    }

    static void display(Event event) {
        println("Start Time : " + event.getStart())
        println("End Time   : " + event.getEnd())
        println("Summary    : " + event.getSummary())
        println("Location   : " + event.getLocation())
        println()
    }
}