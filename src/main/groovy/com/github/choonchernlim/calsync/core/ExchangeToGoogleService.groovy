package com.github.choonchernlim.calsync.core

import com.google.inject.Inject
import org.joda.time.DateTime

/**
 * Class to sync events Exchange to Google Calendar.
 */
class ExchangeToGoogleService {

    UserConfig userConfig
    // ExchangeClient exchangeClient
    GoogleService googleService

    @Inject
    ExchangeToGoogleService(UserConfig userConfig, GoogleService googleService) {
        this.userConfig = userConfig

        // connecting to exchange
//        this.exchangeClient = new ExchangeClient(
//                userConfig.exchangeUserName,
//                userConfig.exchangePassword,
//                userConfig.exchangeUrl)

        this.googleService = googleService
    }

    void run() {
        DateTime startDateTime = DateTime.now().withTimeAtStartOfDay()
        DateTime endDateTime = startDateTime.plusDays(userConfig.totalSyncDays).minusMillis(1)

        // retrieve exchange events
        // List<CalSyncEvent> exchangeEvents = exchangeClient.getEvents(startDateTime, endDateTime)

        // TODO test
        List<CalSyncEvent> exchangeEvents = (0..5).collect {
            new CalSyncEvent(
                    startDateTime: startDateTime,
                    endDateTime: startDateTime.plusHours(it + 1),
                    subject: 'subject ' + it,
                    location: 'location ' + it
            )
        }

        // retrieve google calendar
        String calendarId = googleService.getCalendarId(userConfig.googleCalendarName)

        // retrieve events from google calendar
        List<CalSyncEvent> googleEvents = googleService.getEvents(calendarId, startDateTime, endDateTime)

        // - creating new batch
        // - delete existing google events that no longer match any exchange events
        // - create new google events that exchange events have but google events don't
        // - execute batch
        googleService.
                createBatch().
                batchDeletedEvents(googleEvents.findAll { !exchangeEvents.contains(it) }).
                batchNewEvents(exchangeEvents.findAll { !googleEvents.contains(it) }).
                executeBatch(calendarId)
    }
}