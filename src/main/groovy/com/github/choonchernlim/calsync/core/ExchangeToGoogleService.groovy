package com.github.choonchernlim.calsync.core

import org.joda.time.DateTime

/**
 * Class to sync events Exchange to Google Calendar.
 */
class ExchangeToGoogleService {

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

        // retrieve google calendar
        String calendarId = googleClient.getCalendarId(userConfig.googleCalendarName)

        // retrieve events from google calendar
        List<CalSyncEvent> googleEvents = googleClient.getEvents(calendarId, startDateTime, endDateTime)

        // - creating new batch
        // - delete existing google events that no longer match any exchange events
        // - create new google events that exchange events have but google events don't
        // - execute batch
        googleClient.
                createBatch().
                batchDeletedEvents(calendarId, googleEvents.findAll { !exchangeEvents.contains(it) }).
                batchNewEvents(calendarId, exchangeEvents.findAll { !googleEvents.contains(it) }).
                executeBatch()
    }
}