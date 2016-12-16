package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeEvent
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import spock.lang.Specification
import spock.lang.Unroll

class MapperSpec extends Specification {

    @Unroll
    '#method - given null value, should throw error'() {
        when:
        Mapper."${method}"(null)

        then:
        thrown AssertionError

        where:
        method << [
                'toJodaDateTime',
                'toGoogleDateTime',
                'toAllDayGoogleDateTime',
                'toCalSyncEvent',
                'toGoogleEvent',
                'toCalSyncEvent',
                'isAllDayEvent'
        ]
    }

    def 'toJodaDateTime - given valid datetime, should joda datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()
        def input = new EventDateTime(dateTime: new DateTime(expected.millis))

        when:
        def actual = Mapper.toJodaDateTime(input)

        then:
        actual == expected
    }

    def 'toJodaDateTime - given valid date, should joda datetime'() {
        given:
        def expected = org.joda.time.DateTime.now().withTimeAtStartOfDay()
        def input = new EventDateTime(date: new DateTime(expected.millis))

        when:
        def actual = Mapper.toJodaDateTime(input)

        then:
        actual == expected
    }

    @Unroll
    'toGoogleEventDateTime - given null #var, should throw exception'() {
        when:
        Mapper.toGoogleEventDateTime(isAllDayEvent, jodaDateTime)

        then:
        thrown AssertionError

        where:
        var             | isAllDayEvent | jodaDateTime
        'isAllDayEvent' | null          | org.joda.time.DateTime.now()
        'jodaDateTime'  | true          | null
    }

    def 'toGoogleEventDateTime - given valid non all-day event, should return google event datetime with datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toGoogleEventDateTime(false, expected)

        then:
        actual.getDateTime().getValue() == expected.millis
        actual.getDate() == null
    }

    def 'toGoogleEventDateTime - given valid all-day event, should return google event datetime with date'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toGoogleEventDateTime(true, expected)

        then:
        actual.getDate().getValue() == expected.withTimeAtStartOfDay().millis
        actual.getDateTime() == null
    }

    def 'toGoogleDateTime - given valid value, should return google datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toGoogleDateTime(expected)

        then:
        actual.getValue() == expected.millis
        !actual.dateOnly
    }

    def 'toAllDayGoogleDateTime - given valid value, should return google datetime'() {
        given:
        def expected = org.joda.time.DateTime.now()

        when:
        def actual = Mapper.toAllDayGoogleDateTime(expected)

        then:
        actual.getValue() == expected.withTimeAtStartOfDay().millis
        actual.dateOnly
    }

    def 'toCalSyncEvent - google event - given valid non all-day event, should return calsync event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def input = new Event(
                id: 'id',
                start: Mapper.toGoogleEventDateTime(false, startDateTime),
                end: Mapper.toGoogleEventDateTime(false, endDateTime),
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
        !actual.isAllDayEvent
    }

    def 'toCalSyncEvent - google event - given valid all-day event, should return calsync event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def input = new Event(
                id: 'id',
                start: Mapper.toGoogleEventDateTime(true, startDateTime),
                end: Mapper.toGoogleEventDateTime(true, endDateTime),
                summary: 'summary',
                location: 'location',
                reminders: new Event.Reminders(overrides: [new EventReminder(minutes: 15)])
        )

        when:
        def actual = Mapper.toCalSyncEvent(input)

        then:
        actual.googleEventId == 'id'
        actual.startDateTime == startDateTime.withTimeAtStartOfDay()
        actual.endDateTime == endDateTime.withTimeAtStartOfDay()
        actual.subject == 'summary'
        actual.location == 'location'
        actual.reminderMinutesBeforeStart == 15
        actual.isAllDayEvent
    }

    @Unroll
    'toCalSyncEvent - exchange event - given valid event and includeEventBody == #includeEventBody, should return calsync event'() {
        given:
        def startDateTime = org.joda.time.DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        def input = new ExchangeEvent(
                startDateTime: startDateTime,
                endDateTime: endDateTime,
                subject: 'summary',
                location: 'location',
                reminderMinutesBeforeStart: 15,
                body: 'body',
                isAllDayEvent: true
        )

        when:
        def actual = Mapper.toCalSyncEvent(input, includeEventBody)

        then:
        actual.googleEventId == null
        actual.startDateTime == startDateTime
        actual.endDateTime == endDateTime
        actual.subject == 'summary'
        actual.location == 'location'
        actual.reminderMinutesBeforeStart == 15
        actual.body == expectedBody
        actual.isAllDayEvent

        where:
        includeEventBody | expectedBody
        true             | 'body'
        false            | null
    }


    @Unroll
    'isAllDayEvent - given start #start and end #end, should be #expected'() {
        when:
        def actual = Mapper.isAllDayEvent(new Event(start: start, end: end))

        then:
        actual == expected

        where:
        expected | start                                        | end
        true     | new EventDateTime(date: new DateTime(1))     | new EventDateTime(date: new DateTime(1))
        false    | new EventDateTime(date: new DateTime(1))     | new EventDateTime(dateTime: new DateTime(1))
        false    | new EventDateTime(dateTime: new DateTime(1)) | new EventDateTime(date: new DateTime(1))
        false    | new EventDateTime(dateTime: new DateTime(1)) | new EventDateTime(dateTime: new DateTime(1))
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
                isAllDayEvent: true
        )

        when:
        Event actual = Mapper.toGoogleEvent(input)

        then:
        actual.id == 'id'
        actual.start == Mapper.toGoogleEventDateTime(true, startDateTime)
        actual.end == Mapper.toGoogleEventDateTime(true, endDateTime)
        actual.summary == 'summary'
        actual.location == 'location'

        def reminders = actual.getReminders().getOverrides()
        reminders.size() == 1
        reminders[0].getMinutes() == 15
        reminders[0].getMethod() == 'popup'
    }

    @Unroll
    'toPlainText - given #label value, should be null'() {
        when:
        def actual = Mapper.toPlainText(input)

        then:
        actual == null

        where:
        label   | input
        'null'  | null
        'blank' | ' '
    }

    def 'toPlainText - given valid HTML, should return plain text'() {
        given:
        def input = '<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"><meta name="Generator" content="Microsoft Exchange Server"><!-- converted from rtf --><style><!-- .EmailQuote { margin-left: 1pt; padding-left: 4pt; border-left: #800000 2px solid; } --></style></head><body><font face="Calibri" size="2"><span style="font-size:11pt;"><div>123 / 456</div><div>&nbsp;</div><div>12/9/2016 Added room location – limc</div><div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Room Reservation # 999</div><div>&nbsp;</div><div>&nbsp;</div></span></font></body></html>'

        when:
        def actual = Mapper.toPlainText(input)

        then:
        actual == '''123 / 456 

12/9/2016 Added room location – limc

        Room Reservation # 999'''
    }

    def 'toPlainText - given valid HTML but no content, should return null'() {
        given:
        def input = '<html><div></div></html>'

        when:
        def actual = Mapper.toPlainText(input)

        then:
        actual == null
    }

    def 'humanReadableDateTime - given null value, should throw exception'() {
        when:
        Mapper.humanReadableDateTime(null)

        then:
        thrown AssertionError
    }

    def 'humanReadableDateTime - given valid value, should return formatted datetime'() {
        when:
        def actual = Mapper.humanReadableDateTime(new org.joda.time.DateTime(2016, 12, 12, 9, 10, 11, 12))

        then:
        actual == 'Dec 12 @ 09:10 AM'
    }
}
