package com.github.choonchernlim.calsync.exchange

import com.github.choonchernlim.calsync.core.Mapper
import com.github.choonchernlim.calsync.core.UserConfig
import groovy.transform.PackageScope
import microsoft.exchange.webservices.data.core.ExchangeService
import microsoft.exchange.webservices.data.core.PropertySet
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
@PackageScope
class ExchangeClient {
    private static Logger LOGGER = LoggerFactory.getLogger(ExchangeClient)

    final ExchangeService service

    ExchangeClient() {
        this.service = new ExchangeService()
    }

    void init(UserConfig userConfig) {
        assert userConfig

        this.service.setCredentials(new WebCredentials(userConfig.exchangeUserName, userConfig.exchangePassword))
        this.service.setUrl(new URI(userConfig.exchangeUrl))
    }

    /**
     * Returns events based on the given datetime range.
     *
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @return Events
     */
    List<ExchangeEvent> getEvents(DateTime startDateTime, DateTime endDateTime) {
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime

        return CalendarFolder.bind(service, WellKnownFolderName.Calendar).
                findAppointments(new CalendarView(startDateTime.toDate(), endDateTime.toDate())).
                getItems()?.
                collect { appointment ->
                    appointment.load(PropertySet.firstClassProperties)
                    Mapper.toExchangeEvent(appointment)
                } ?: []
    }


}

