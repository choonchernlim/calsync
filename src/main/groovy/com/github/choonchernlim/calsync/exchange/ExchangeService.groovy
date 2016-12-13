package com.github.choonchernlim.calsync.exchange

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.github.choonchernlim.calsync.core.Mapper
import com.github.choonchernlim.calsync.core.UserConfig
import com.google.inject.Inject
import microsoft.exchange.webservices.data.core.service.item.Appointment
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

    void init(UserConfig userConfig) {
        assert userConfig

        exchangeClient.init(userConfig)
    }

    /**
     * Returns events based on the given datetime range.
     *
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @param includeCanceledEvents Whether to include canceled events or not
     * @return Events if there's any, otherwise empty list
     */
    List<CalSyncEvent> getEvents(DateTime startDateTime, DateTime endDateTime, Boolean includeCanceledEvents) {
        assert startDateTime && endDateTime && startDateTime <= endDateTime
        assert includeCanceledEvents != null

        LOGGER.info("Retrieving events from ${startDateTime} to ${endDateTime}...")

        List<Appointment> exchangeEvents = exchangeClient.getEvents(startDateTime, endDateTime) ?: []

        LOGGER.info("\tTotal events found: ${exchangeEvents.size()}...")

        if (!includeCanceledEvents) {
            exchangeEvents = exchangeEvents.findAll { !it.isCancelled }
            LOGGER.info("\tTotal events after excluding canceled events: ${exchangeEvents.size()}...")
        }

        return exchangeEvents.collect { Mapper.toCalSyncEvent(it) }
    }
}

