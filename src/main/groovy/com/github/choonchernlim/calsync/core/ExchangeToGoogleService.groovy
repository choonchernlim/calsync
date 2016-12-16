package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeService
import com.github.choonchernlim.calsync.google.GoogleService
import com.google.inject.Inject
import org.joda.time.DateTime

/**
 * Class to sync events Exchange to Google Calendar.
 */
class ExchangeToGoogleService {

    ExchangeService exchangeService
    GoogleService googleService
    DateTimeNowSupplier dateTimeNowSupplier

    @Inject
    ExchangeToGoogleService(
            ExchangeService exchangeService,
            GoogleService googleService,
            DateTimeNowSupplier dateTimeNowSupplier) {
        this.exchangeService = exchangeService
        this.googleService = googleService
        this.dateTimeNowSupplier = dateTimeNowSupplier
    }

    void run(UserConfig userConfig) {
        assert userConfig

        DateTime dateTimeNow = dateTimeNowSupplier.get()
        DateTime startDateTime = dateTimeNow.withTimeAtStartOfDay()
        DateTime endDateTime = startDateTime.plusDays(userConfig.totalSyncDays).minusMillis(1)

        exchangeService.init(userConfig)
        googleService.init(userConfig)

        // retrieve exchange events
        List<CalSyncEvent> exchangeEvents = exchangeService.getEvents(
                startDateTime,
                endDateTime,
                userConfig.includeCanceledEvents,
                userConfig.includeEventBody)

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
                batchNewEvents(exchangeEvents.findAll { !googleEvents.contains(it) } << new CalSyncEvent(
                        subject: "CalSync - Last Sync: ${Mapper.humanReadableDateTime(dateTimeNow)}",
                        startDateTime: startDateTime,
                        endDateTime: startDateTime.plusDays(1),
                        isAllDayEvent: true
                )).
                executeBatch(calendarId)
    }
}