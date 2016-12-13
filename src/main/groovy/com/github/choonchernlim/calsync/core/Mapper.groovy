package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeEvent
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import microsoft.exchange.webservices.data.core.service.item.Appointment
import microsoft.exchange.webservices.data.property.complex.MessageBody
import org.apache.commons.lang3.StringEscapeUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist

/**
 * Utility class to map one object type to another.
 */
class Mapper {
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM dd '@' hh:mm a")

    /**
     * Maps Google EventDateTime to Joda DateTime.
     *
     * @param eventDateTime Google EventDateTime
     * @return Joda DateTime
     */
    static org.joda.time.DateTime toJodaDateTime(EventDateTime eventDateTime) {
        assert eventDateTime

        return new org.joda.time.DateTime(eventDateTime.getDateTime().getValue())
    }

    /**
     * Maps Joda DateTime to Google EventDateTime.
     *
     * @param jodaDateTime Joda DateTime
     * @return Google EventDateTime
     */
    static EventDateTime toGoogleEventDateTime(org.joda.time.DateTime jodaDateTime) {
        assert jodaDateTime

        return new EventDateTime(dateTime: toGoogleDateTime(jodaDateTime))
    }

    /**
     * Maps Joda DateTime to Google DateTime.
     *
     * @param jodaDateTime Joda DateTime
     * @return Google DateTime
     */
    static DateTime toGoogleDateTime(org.joda.time.DateTime jodaDateTime) {
        assert jodaDateTime

        return new DateTime(jodaDateTime.millis)
    }

    /**
     * Maps Google Event to CalSyncEvent.
     *
     * @param event Google Event
     * @return CalSyncEvent
     */
    static CalSyncEvent toCalSyncEvent(Event event) {
        assert event

        return new CalSyncEvent(
                googleEventId: event.getId(),
                startDateTime: toJodaDateTime(event.getStart()),
                endDateTime: toJodaDateTime(event.getEnd()),
                subject: event.getSummary(),
                location: event.getLocation(),
                reminderMinutesBeforeStart: event.getReminders()?.getOverrides()?.get(0)?.getMinutes(),
                body: event.getDescription() ?: null
        )
    }

    /**
     * Maps Exchange Event to CalSyncEvent.
     *
     * @param exchangeEvent Exchange Event
     * @param includeEventBody Whether to include event body or not
     * @return CalSyncEvent
     */
    static CalSyncEvent toCalSyncEvent(ExchangeEvent exchangeEvent, Boolean includeEventBody) {
        assert exchangeEvent
        assert includeEventBody != null

        return new CalSyncEvent(
                startDateTime: exchangeEvent.startDateTime,
                endDateTime: exchangeEvent.endDateTime,
                subject: exchangeEvent.subject,
                location: exchangeEvent.location,
                reminderMinutesBeforeStart: exchangeEvent.reminderMinutesBeforeStart,
                body: includeEventBody ? exchangeEvent.body : null
        )
    }

    /**
     * Maps CalSyncEvent to Google Event.
     *
     * @param calSyncEvent CalSyncEvent
     * @return Google Event
     */
    static Event toGoogleEvent(CalSyncEvent calSyncEvent) {
        assert calSyncEvent

        // only create reminder if there's one
        def reminders = calSyncEvent.reminderMinutesBeforeStart ?
                new Event.Reminders(
                        useDefault: false,
                        overrides: [
                                new EventReminder(
                                        method: 'popup',
                                        minutes: calSyncEvent.reminderMinutesBeforeStart
                                )
                        ]
                ) : null

        return new Event(
                id: calSyncEvent.googleEventId,
                start: toGoogleEventDateTime(calSyncEvent.startDateTime),
                end: toGoogleEventDateTime(calSyncEvent.endDateTime),
                summary: calSyncEvent.subject,
                location: calSyncEvent.location,
                reminders: reminders,
                description: calSyncEvent.body
        )
    }

    /**
     * Maps Appointment to ExchangeEvent.
     *
     * @param appointment Appointment
     * @return ExchangeEvent
     */
    // TODO not testable ATM, not sure how to mock Appointment to return `body` data
    static ExchangeEvent toExchangeEvent(Appointment appointment) {
        assert appointment

        return new ExchangeEvent(
                startDateTime: new org.joda.time.DateTime(appointment.start),
                endDateTime: new org.joda.time.DateTime(appointment.end),
                subject: appointment.subject,
                location: appointment.location,
                reminderMinutesBeforeStart: appointment.reminderMinutesBeforeStart,
                body: toPlainText(MessageBody.getStringFromMessageBody(appointment.body)),
                isCanceled: appointment.isCancelled
        )
    }

    /**
     * Transforms HTML text to plain text.
     *
     * @param html HTML text
     * @return Plain text
     */
    // http://stackoverflow.com/questions/5640334/how-do-i-preserve-line-breaks-when-using-jsoup-to-convert-html-to-plain-text
    static String toPlainText(String html) {
        if (!html?.trim()) {
            return null
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false)

        Document document = Jsoup.parse(html)
        document.outputSettings(outputSettings)

        document.select('br').append('\\n')
        document.select('p').prepend('\\n\\n')

        // Exchange tends to have <div>&nbsp;</div> as line separator
        document.select('div:contains( )').prepend('\\n\\n')

        String sanitizedHtml = document.html().replaceAll('\\\\n', '\n').replaceAll('&nbsp;', ' ')

        return StringEscapeUtils.unescapeHtml4(Jsoup.clean(sanitizedHtml, '', Whitelist.none(), outputSettings)).
                trim() ?: null
    }

    /**
     * Returns human readable datetime.
     *
     * @param dateTime Joda time
     * @return Datetime string
     */
    static String humanReadableDateTime(org.joda.time.DateTime dateTime) {
        assert dateTime

        return dateTimeFormatter.print(dateTime)
    }
}
