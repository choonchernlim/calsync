package com.github.choonchernlim.calsync.core

import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Class to sync events Exchange to Google Calendar.
 */
class ExchangeToGoogleService {
    private static Logger LOGGER = LoggerFactory.getLogger(ExchangeToGoogleService)

    UserConfig userConfig
    ExchangeClient exchangeClient
    GoogleClient googleClient

    ExchangeToGoogleService(UserConfig userConfig) {
        this.userConfig = userConfig

        // connecting to exchange
        this.exchangeClient = new ExchangeClient(
                userConfig.exchangeUserName,
                userConfig.exchangePassword,
                userConfig.exchangeUrl)

        // connecting to google
        this.googleClient = new GoogleClient(userConfig.googleClientSecretJsonFilePath)
    }

    void run() {
        DateTime startDateTime = DateTime.now().withTimeAtStartOfDay()
        DateTime endDateTime = startDateTime.plusDays(userConfig.totalSyncDays).minusMillis(1)

        // retrieve exchange events
        List<CalSyncEvent> exchangeEvents = exchangeClient.getEvents(startDateTime, endDateTime)

        exchangeEvents.each {
            LOGGER.info("Exchange events: ${it}")
        }

        // retrieve google calendar
        String calendarId = googleClient.getCalendarId(userConfig.googleCalendarName)

        // retrieve events from google calendar
        List<CalSyncEvent> googleEvents = googleClient.getEvents(calendarId, startDateTime, endDateTime)

        googleEvents.each {
            LOGGER.info("Google events: ${it}")
        }

        // TODO need to make this asynchronous!
        // googleClient.deleteEvents(calendarId, googleEvents)

        // delete existing google events that no longer match any exchange events
        List<CalSyncEvent> outdatedGoogleEvents = googleEvents.findAll { !exchangeEvents.contains(it) }
        if (outdatedGoogleEvents) {
            outdatedGoogleEvents.each {
                LOGGER.info("Deleting outdated Google events: ${it}")
            }

            googleClient.deleteEvents(calendarId, outdatedGoogleEvents)
        }

        // create new google events that exchange events have but google events don't
        List<CalSyncEvent> newGoogleEvents = exchangeEvents.findAll { !googleEvents.contains(it) }
        if (newGoogleEvents) {
            newGoogleEvents.each {
                LOGGER.info("Creating new Google events: ${it}")
            }

            googleClient.createEvents(calendarId, newGoogleEvents)
        }
    }
}