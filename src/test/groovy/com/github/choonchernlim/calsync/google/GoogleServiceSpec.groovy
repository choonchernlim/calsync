package com.github.choonchernlim.calsync.google

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.github.choonchernlim.calsync.core.UserConfig
import com.google.api.services.calendar.model.*
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class GoogleServiceSpec extends Specification {

    def googleClient = Mock GoogleClient
    def service = new GoogleService(googleClient)

    def 'init - given null config, should throw exception'() {
        when:
        service.init(null)

        then:
        0 * _

        thrown AssertionError
    }

    def 'init - given config, should pass config to client'() {
        given:
        def userConfig = new UserConfig()

        when:
        service.init(userConfig)

        then:
        1 * googleClient.init(userConfig)
        0 * _
    }

    @Unroll
    def 'getCalendarId - given #label calendar name, should throw exception'() {
        when:
        service.getCalendarId(input)

        then:
        0 * _
        thrown AssertionError

        where:
        label   | input
        'null'  | null
        'blank' | ' '
    }

    def 'getCalendarId - given new calendar, should create one first'() {
        when:
        def id = service.getCalendarId('new')

        then:
        1 * googleClient.getCalendarList() >> new CalendarList(items: null)
        1 * googleClient.createCalendar() >> new Calendar(id: '1')
        0 * _

        id == '1'
    }

    def 'getCalendarId - given existing calendar, should not create new calendar'() {
        when:
        def id = service.getCalendarId('existing')

        then:
        1 * googleClient.getCalendarList() >> new CalendarList(items: [
                new CalendarListEntry(
                        id: '1',
                        summary: 'existing'
                )])
        0 * _

        id == '1'
    }

    def 'createBatch - when invoked, should clear event action list'() {
        given:
        service.eventActions.add(new EventAction())

        when:
        service.createBatch()

        then:
        service.eventActions.isEmpty()
    }

    @Unroll
    def 'executeBatch - given #label calendar ID, should throw exception'() {
        when:
        service.executeBatch(input)

        then:
        0 * _
        thrown AssertionError

        where:
        label   | input
        'null'  | null
        'blank' | ' '
    }

    def 'executeBatch - when empty event action list, should not execute batch'() {
        when:
        service.executeBatch('1')

        then:
        0 * _
    }

    def 'executeBatch - when non-empty event action list, should not execute batch'() {
        given:
        service.eventActions.add(new EventAction())

        when:
        service.executeBatch('1')

        then:
        1 * googleClient.executeActions('1', service.eventActions)
        0 * _
    }

    def 'batchNewEvents - given null events, should throw exception'() {
        when:
        service.batchNewEvents(null)

        then:
        0 * _
        thrown AssertionError
    }

    def 'batchNewEvents - when empty event list, should not add to event action list'() {
        when:
        service.batchNewEvents([])

        then:
        0 * _
    }

    def 'batchNewEvents - when non-empty event list, should add to event action list'() {
        given:
        assert service.eventActions.size() == 0
        def event = new CalSyncEvent(
                startDateTime: DateTime.now(),
                endDateTime: DateTime.now().plusDays(1),
                subject: 'subject',
                location: 'location'
        )

        when:
        service.batchNewEvents([event])

        then:
        0 * _

        assert service.eventActions.size() == 1
        assert service.eventActions*.action == [EventAction.Action.INSERT]
        assert service.eventActions*.event == [event]
    }

    def 'batchDeletedEvents - given null events, should throw exception'() {
        when:
        service.batchDeletedEvents(null)

        then:
        0 * _
        thrown AssertionError
    }

    def 'batchDeletedEvents - when empty event list, should not add to event action list'() {
        when:
        service.batchDeletedEvents([])

        then:
        0 * _
    }

    def 'batchDeletedEvents - when non-empty event list, should add to event action list'() {
        given:
        assert service.eventActions.size() == 0
        def event = new CalSyncEvent(
                startDateTime: DateTime.now(),
                endDateTime: DateTime.now().plusDays(1),
                subject: 'subject',
                location: 'location'
        )

        when:
        service.batchDeletedEvents([event])

        then:
        0 * _

        assert service.eventActions.size() == 1
        assert service.eventActions*.action == [EventAction.Action.DELETE]
        assert service.eventActions*.event == [event]
    }

    @Unroll
    def 'getEvents - given #label, should throw exception'() {
        when:
        service.getEvents(calendarId, startDateTime, endDateTime)

        then:
        0 * _
        thrown AssertionError

        where:
        label                         | calendarId | startDateTime  | endDateTime
        'calendarId == null'          | null       | DateTime.now() | DateTime.now().minusDays(1)
        'calendarId == blank'         | ' '        | DateTime.now() | DateTime.now().minusDays(1)
        'startDateTime == null'       | '1'        | null           | DateTime.now().minusDays(1)
        'endDateTime == null'         | '1'        | DateTime.now() | null
        'startDateTime > endDateTime' | '1'        | DateTime.now() | DateTime.now().minusDays(1)
    }

    def 'getEvents - given no events, should return empty list'() {
        given:
        def calendarId = '1'
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(calendarId, startDateTime, endDateTime)

        then:
        1 * googleClient.getEvents(calendarId, startDateTime, endDateTime) >> new Events(items: null)
        0 * _

        events.isEmpty()
    }

    def 'getEvents - given events found, should return events'() {
        given:
        def calendarId = '1'
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(calendarId, startDateTime, endDateTime)

        then:
        1 * googleClient.getEvents(calendarId, startDateTime, endDateTime) >> new Events(items: [
                new Event(
                        id: '2',
                        start: new EventDateTime(dateTime: new com.google.api.client.util.DateTime(new Date())),
                        end: new EventDateTime(dateTime: new com.google.api.client.util.DateTime(new Date())),
                        summary: 'summary'
                )
        ])
        0 * _

        events.size() == 1
        events*.googleEventId == ['2']
    }

    @Unroll
    def 'deleteCalendar - given #label calendar ID, should throw exception'() {
        when:
        service.deleteCalendar(calendarId)

        then:
        0 * _
        thrown AssertionError

        where:
        label   | calendarId
        'null'  | null
        'blank' | ' '
    }

    def 'deleteCalendar - given non-blank calendar ID, should delete calendar'() {
        when:
        service.deleteCalendar('1')

        then:
        1 * googleClient.deleteCalendar('1')
        0 * _
    }
}

