package com.github.choonchernlim.calsync.core

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
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

    @Unroll
    def 'toPlainText - given #label value, should be null'() {
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
