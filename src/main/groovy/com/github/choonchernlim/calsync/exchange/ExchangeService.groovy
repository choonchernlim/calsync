package com.github.choonchernlim.calsync.exchange

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.github.choonchernlim.calsync.core.Mapper
import com.google.inject.Inject
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Exchange client class.
 */
class ExchangeService {
    private static Logger LOGGER = LoggerFactory.getLogger(ExchangeService)

    final ExchangeClient exchangeClient

    @Inject
    ExchangeService(ExchangeClient exchangeClient) {
        this.exchangeClient = exchangeClient
    }

    /**
     * Returns events based on the given datetime range.
     *
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @return Events if there's any, otherwise empty list
     */
    List<CalSyncEvent> getEvents(DateTime startDateTime, DateTime endDateTime) {
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime

        LOGGER.info("Retrieving events from ${startDateTime} to ${endDateTime}...")

        List<CalSyncEvent> events = exchangeClient.
                getEvents(startDateTime, endDateTime)?.
                collect { Mapper.toCalSyncEvent(it) } ?: []

        LOGGER.info("Total events found: ${events.size()}...")

        return events
    }
}

