package com.github.choonchernlim.calsync.core

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import microsoft.exchange.webservices.data.core.ExchangeService
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion
import microsoft.exchange.webservices.data.core.service.item.Appointment
import spock.lang.Specification
import spock.lang.Unroll

class MapperSpec extends Specification {

    @Unroll
    def '#method - given null value, should throw error'() {
        when:
        Mapper."${method}"(null)

        then:
        thrown AssertionError

        where:
        method << [
                'toJodaDateTime',
                'toGoogleEventDateTime',
                'toGoogleDateTime',
                'toCalSyncEvent',
                'toGoogleEvent',
                'toCalSyncEvent'
        ]
    }

    def 'toJodaDateTime - given valid value, should joda datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()
        def input = new EventDateTime(dateTime: new DateTime(expected.millis))

        when:
        def actual = Mapper.toJodaDateTime(input)

        then:
        actual == expected
    }

    def 'toGoogleEventDateTime - given valid value, should return google event datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toGoogleEventDateTime(expected)

        then:
        actual.getDateTime().getValue() == expected.millis
    }

    def 'toGoogleDateTime - given valid value, should return google datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toGoogleDateTime(expected)

        then:
        actual.getValue() == expected.millis
    }

    def 'toCalSyncEvent - event - given valid value, should return calsync event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def input = new Event(
                id: 'id',
                start: Mapper.toGoogleEventDateTime(startDateTime),
                end: Mapper.toGoogleEventDateTime(endDateTime),
                summary: 'summary',
                location: 'location',
                reminders: new Event.Reminders(overrides: [new EventReminder(minutes: 15)])

        )

        when:
        def actual = Mapper.toCalSyncEvent(input)

        then:
        actual.googleEventId == 'id'
        actual.startDateTime == startDateTime
        actual.endDateTime == endDateTime
        actual.subject == 'summary'
        actual.location == 'location'
        actual.reminderMinutesBeforeStart == 15
    }

    def 'toCalSyncEvent - appointment - given valid value, should return calsync event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def exchangeService = Mock(ExchangeService) {
            getRequestedServerVersion() >> ExchangeVersion.Exchange2010_SP2
        }
        def appointment = new Appointment(exchangeService)

        appointment.start = startDateTime.toDate()
        appointment.end = endDateTime.toDate()
        appointment.subject = 'summary'
        appointment.location = 'location'
        appointment.reminderMinutesBeforeStart = 15

        when:
        def actual = Mapper.toCalSyncEvent(appointment)

        then:
        actual.googleEventId == null
        actual.startDateTime == startDateTime
        actual.endDateTime == endDateTime
        actual.subject == 'summary'
        actual.location == 'location'
        actual.reminderMinutesBeforeStart == 15
    }

    def 'toGoogleEvent - given valid value, should return google event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def input = new CalSyncEvent(
                googleEventId: 'id',
                startDateTime: startDateTime,
                endDateTime: endDateTime,
                subject: 'summary',
                location: 'location',
                reminderMinutesBeforeStart: 15,
                )

        when:
        Event actual = Mapper.toGoogleEvent(input)

        then:
        actual.id == 'id'
        actual.start == Mapper.toGoogleEventDateTime(startDateTime)
        actual.end == Mapper.toGoogleEventDateTime(endDateTime)
        actual.summary == 'summary'
        actual.location == 'location'

        def reminders = actual.getReminders().getOverrides()
        reminders.size() == 1
        reminders[0].getMinutes() == 15
        reminders[0].getMethod() == 'popup'
    }
}
