package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeService
import com.github.choonchernlim.calsync.google.GoogleService
import org.joda.time.DateTime
import spock.lang.Specification

class ExchangeToGoogleServiceSpec extends Specification {

    def googleCalendarId = 'googleCalendarId'
    def googleCalendarName = 'googleCalendarName'

    def exchangeService = Mock ExchangeService
    def googleService = Mock GoogleService
    def dateTimeNowSupplier = Mock DateTimeNowSupplier

    def dateTime = new DateTime(2016, 12, 1, 3, 4, 5, 6)
    def startDateTime = new DateTime(2016, 12, 1, 0, 0, 0, 0)
    def endDateTime = new DateTime(2016, 12, 1, 23, 59, 59, 999)

    def lastSyncEvent = new CalSyncEvent(
            subject: "CalSync - Last Sync: ${Mapper.humanReadableDateTime(dateTime)}",
            startDateTime: startDateTime,
            endDateTime: startDateTime.plusDays(1),
            isAllDayEvent: true
    )

    ExchangeToGoogleService service = new ExchangeToGoogleService(exchangeService, googleService, dateTimeNowSupplier)

    def userConfig = new UserConfig(
            exchangeUserName: 'exchangeUserName',
            exchangePassword: 'exchangePassword',
            exchangeUrl: 'exchangeUrl',
            googleClientSecretJsonFilePath: 'googleClientSecretJsonFilePath',
            googleCalendarName: googleCalendarName,
            totalSyncDays: 1,
            nextSyncInMinutes: 15,
            includeCanceledEvents: true,
            includeEventBody: true
    )

    def 'run - given no exchange events and no existing google events, should do nothing'() {
        when:
        service.run(userConfig)

        then:
        1 * dateTimeNowSupplier.get() >> dateTime
        1 * exchangeService.init(userConfig)
        1 * googleService.init(userConfig)
        1 * exchangeService.getEvents(startDateTime, endDateTime, true, true) >> []
        1 * googleService.getCalendarId(googleCalendarName) >> googleCalendarId
        1 * googleService.getEvents(googleCalendarId, startDateTime, endDateTime) >> []
        1 * googleService.createBatch() >> googleService
        1 * googleService.batchDeletedEvents([]) >> googleService
        1 * googleService.batchNewEvents([lastSyncEvent]) >> googleService
        1 * googleService.executeBatch(googleCalendarId)
        0 * _
    }

    def 'run - given no exchange events but has existing google events, should delete google events'() {
        given:
        def googleEvents = [new CalSyncEvent(subject: 'subject1'), new CalSyncEvent(subject: 'subject2')]

        when:
        service.run(userConfig)

        then:
        1 * dateTimeNowSupplier.get() >> dateTime
        1 * exchangeService.init(userConfig)
        1 * googleService.init(userConfig)
        1 * exchangeService.getEvents(startDateTime, endDateTime, true, true) >> []
        1 * googleService.getCalendarId(googleCalendarName) >> googleCalendarId
        1 * googleService.getEvents(googleCalendarId, startDateTime, endDateTime) >> googleEvents
        1 * googleService.createBatch() >> googleService
        1 * googleService.batchDeletedEvents(googleEvents) >> googleService
        1 * googleService.batchNewEvents([lastSyncEvent]) >> googleService
        1 * googleService.executeBatch(googleCalendarId)
        0 * _
    }

    def 'run - given exchange events but no google events, should create google events'() {
        given:
        def exchangeEvents = [new CalSyncEvent(subject: 'subject1'), new CalSyncEvent(subject: 'subject2')]

        when:
        service.run(userConfig)

        then:
        1 * dateTimeNowSupplier.get() >> dateTime
        1 * exchangeService.init(userConfig)
        1 * googleService.init(userConfig)
        1 * exchangeService.getEvents(startDateTime, endDateTime, true, true) >> exchangeEvents
        1 * googleService.getCalendarId(googleCalendarName) >> googleCalendarId
        1 * googleService.getEvents(googleCalendarId, startDateTime, endDateTime) >> []
        1 * googleService.createBatch() >> googleService
        1 * googleService.batchDeletedEvents([]) >> googleService
        1 * googleService.batchNewEvents(exchangeEvents.collect() << lastSyncEvent) >> googleService
        1 * googleService.executeBatch(googleCalendarId)
        0 * _
    }

    def 'run - given exchange events and google events, should delete outdated google events and create new events'() {
        given:
        def event1 = new CalSyncEvent(subject: 'subject1')
        def event2 = new CalSyncEvent(subject: 'subject2')
        def event3 = new CalSyncEvent(subject: 'subject3')

        def googleEvents = [event1, event3]
        def exchangeEvents = [event1, event2]
        def googleEventsToBeDeleted = [event3]
        def googleEventsToBeAdded = [event2]

        when:
        service.run(userConfig)

        then:
        1 * dateTimeNowSupplier.get() >> dateTime
        1 * exchangeService.init(userConfig)
        1 * googleService.init(userConfig)
        1 * exchangeService.getEvents(startDateTime, endDateTime, true, true) >> exchangeEvents
        1 * googleService.getCalendarId(googleCalendarName) >> googleCalendarId
        1 * googleService.getEvents(googleCalendarId, startDateTime, endDateTime) >> googleEvents
        1 * googleService.createBatch() >> googleService
        1 * googleService.batchDeletedEvents(googleEventsToBeDeleted) >> googleService
        1 * googleService.batchNewEvents(googleEventsToBeAdded << lastSyncEvent) >> googleService
        1 * googleService.executeBatch(googleCalendarId)
        0 * _
    }
}
