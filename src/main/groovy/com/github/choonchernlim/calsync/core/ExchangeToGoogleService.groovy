package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeService
import com.github.choonchernlim.calsync.google.GoogleService
import com.google.inject.Inject
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Class to sync events Exchange to Google Calendar.
 */
class ExchangeToGoogleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeToGoogleService)

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
        DateTime startDateTime = dateTimeNow.minusDays(userConfig.totalSyncDaysPast).withTimeAtStartOfDay()
        DateTime endDateTime = dateTimeNow.withTimeAtStartOfDay().plusDays(userConfig.totalSyncDays).minusMillis(1)

        exchangeService.init(userConfig)
        googleService.init(userConfig)

        List<CalSyncEvent> exchangeEvents = []
        try {
            // retrieve exchange events
            exchangeEvents = exchangeService.getEvents(
                    startDateTime,
                    endDateTime,
                    userConfig.includeCanceledEvents,
                    userConfig.includeEventBody)
        }
        catch (ServiceRequestException e) {
            // on connection exception, suppress exception if user says so
            if (ExceptionUtils.getStackTrace(e).contains('java.net.ConnectException') &&
                userConfig.exchangeSleepOnConnectionError) {
                LOGGER.error(e.getMessage())
                return
            }

            // otherwise, throw exception
            throw e
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
                batchNewEvents(exchangeEvents.findAll { !googleEvents.contains(it) } << new CalSyncEvent(
                        subject: "CalSync - Last Sync: ${Mapper.humanReadableDateTime(dateTimeNow)}",
                        startDateTime: dateTimeNow.withTimeAtStartOfDay(),
                        endDateTime: dateTimeNow.withTimeAtStartOfDay().plusDays(1),
                        isAllDayEvent: true
                )).
                executeBatch(calendarId)
    }
}