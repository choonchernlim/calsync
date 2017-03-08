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

/**
 * Exchange client class.
 */
@PackageScope
class ExchangeClient {
    // private static Logger LOGGER = LoggerFactory.getLogger(ExchangeClient)

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
                findAll { appointment ->
                    // Exchange sets all-day event appointment from given day midnight to the next day midnight,
                    // which may get picked up because it matches the given `endDateTime` even though it is an
                    // all-day event appointment for the day before.
                    //
                    // To fix this, only include appointments with start datetime between the given
                    // date range.
                    if (appointment.isAllDayEvent) {
                        def appointmentStartDateTime = new DateTime(appointment.start)

                        return !appointmentStartDateTime.isBefore(startDateTime) &&
                               !appointmentStartDateTime.isAfter(endDateTime)
                    }
                    else {
                        return appointment
                    }
                }?.
                collect { appointment ->
                    appointment.load(PropertySet.firstClassProperties)
                    Mapper.toExchangeEvent(appointment)
                } ?: []
    }


}

