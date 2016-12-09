package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeClient
import com.github.choonchernlim.calsync.google.GoogleClient
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main)

    static void main(String[] args) {
        UserConfig userConfig = new UserConfig()

        DateTime startDateTime = DateTime.now().withTimeAtStartOfDay()
        DateTime endDateTime = startDateTime.plusDays(1).minusMillis(1)

        ExchangeClient exchangeClient = new ExchangeClient(
                userConfig.exchangeUserName,
                userConfig.exchangePassword,
                userConfig.exchangeUrl)

        GoogleClient googleClient = new GoogleClient(userConfig.googleClientSecretJsonFilePath)

        List<CalSyncEvent> exchangeEvents = exchangeClient.getEvents(startDateTime, endDateTime)

        exchangeEvents.each {
            LOGGER.info("Exchange events: ${it}")
        }

        String calendarId = googleClient.getCalendarId(userConfig.googleCalendarName)

        List<CalSyncEvent> googleEvents = googleClient.getEvents(calendarId, startDateTime, endDateTime)

        googleEvents.each {
            LOGGER.info("Google events: ${it}")
        }

        List<CalSyncEvent> outdatedGoogleEvents = googleEvents.findAll { !exchangeEvents.contains(it) }
        if (outdatedGoogleEvents) {
            outdatedGoogleEvents.each {
                LOGGER.info("Deleting outdated Google events: ${it}")
            }

            googleClient.deleteEvents(calendarId, outdatedGoogleEvents)
        }

        List<CalSyncEvent> newGoogleEvents = exchangeEvents.findAll { !googleEvents.contains(it) }

        if (newGoogleEvents) {
            newGoogleEvents.each {
                LOGGER.info("Creating new Google events: ${it}")
            }

            googleClient.createEvents(calendarId, newGoogleEvents)
        }

        //  googleClient.createEvents(calendarId, exchangeEvents)

        // googleClient.deleteEvents(calendarId, events)

        // googleClient.deleteCalendar(calendarId)
    }
}