package com.github.choonchernlim.calsync.core

import microsoft.exchange.webservices.data.core.ExchangeService
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder
import microsoft.exchange.webservices.data.credential.WebCredentials
import microsoft.exchange.webservices.data.search.CalendarView
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Exchange client class.
 */
class ExchangeClient {
    private static Logger LOGGER = LoggerFactory.getLogger(ExchangeClient)

    ExchangeService service

    ExchangeClient(String userName, String password, String url) {
        LOGGER.info('Authenticating against Exchange...')

        this.service = new ExchangeService()
        this.service.setCredentials(new WebCredentials(userName, password))
        this.service.setUrl(new URI(url))
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

        List<CalSyncEvent> events = CalendarFolder.bind(service, WellKnownFolderName.Calendar).
                findAppointments(new CalendarView(startDateTime.toDate(), endDateTime.toDate())).
                getItems()?.
                collect { MapperUtils.toCalSyncEvent(it) } ?: []

        LOGGER.info("Total events found: ${events.size()}...")

        return events
    }
}

