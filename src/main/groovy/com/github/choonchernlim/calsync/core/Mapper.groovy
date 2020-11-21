package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeEvent
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import microsoft.exchange.webservices.data.core.enumeration.property.LegacyFreeBusyStatus
import microsoft.exchange.webservices.data.core.enumeration.property.MeetingResponseType
import microsoft.exchange.webservices.data.core.service.item.Appointment
import microsoft.exchange.webservices.data.property.complex.AttendeeCollection
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

        // Google uses `datetime` for non-all-day event and `date` (YYYY-MM-DD) for all-day event
        return eventDateTime.getDateTime() ?
                new org.joda.time.DateTime(eventDateTime.getDateTime().getValue()) :

                // Need to parse string instead of using new DateTime(long).
                // Otherwise, 2016-12-15 becomes 2016-12-14T18:00:00.000-06:00
                org.joda.time.DateTime.parse(eventDateTime.getDate().toString())
    }

    /**
     * Maps Joda DateTime to Google EventDateTime.
     *
     * @param isAllDayEvent Whether it is all-day event or not
     * @param jodaDateTime Joda DateTime
     * @return Google EventDateTime
     */
    static EventDateTime toGoogleEventDateTime(Boolean isAllDayEvent, org.joda.time.DateTime jodaDateTime) {
        assert isAllDayEvent != null
        assert jodaDateTime

        return isAllDayEvent ?
                new EventDateTime(date: toAllDayGoogleDateTime(jodaDateTime)) :
                new EventDateTime(dateTime: toGoogleDateTime(jodaDateTime))
    }

    /**
     * Maps Joda DateTime to all-day Google DateTime.
     *
     * @param jodaDateTime Joda DateTime
     * @return All-day Google DateTime
     */
    static DateTime toAllDayGoogleDateTime(org.joda.time.DateTime jodaDateTime) {
        assert jodaDateTime

        return new DateTime(true, jodaDateTime.withTimeAtStartOfDay().millis, null)
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

        def attendees = event.getAttendees()
                .collect {
                    def response = CalSyncEvent.Attendee.Response.NO_RESPONSE
                    if (it.getResponseStatus() == "accepted") {
                        response = CalSyncEvent.Attendee.Response.ACCEPTED
                    } else if (it.getResponseStatus() == "declined") {
                        response = CalSyncEvent.Attendee.Response.DECLINED
                    } else if (it.getResponseStatus() == "tentative") {
                        response = CalSyncEvent.Attendee.Response.TENTATIVE
                    }

                    new CalSyncEvent.Attendee(
                            address: it.getEmail().toLowerCase(), // address might be lowercased by Google
                            name: it.getDisplayName(),
                            response: response,
                            isOptional: it.isOptional()
                    )
                }
                .sort { it.address } // sort because Google might return attendees in a different order

        return new CalSyncEvent(
                googleEventId: event.getId(),
                startDateTime: toJodaDateTime(event.getStart()),
                endDateTime: toJodaDateTime(event.getEnd()),
                subject: event.getSummary(),
                location: event.getLocation(),
                reminderMinutesBeforeStart: event.getReminders()?.getOverrides()?.get(0)?.getMinutes(),
                body: event.getDescription() ?: null,
                isAllDayEvent: isAllDayEvent(event),
                attendees: attendees,
                organizerAddress: event.getOrganizer().getEmail().toLowerCase(), // address might be lowercased by Google
                organizerName: event.getOrganizer().getDisplayName(),
                isBusy: event.getTransparency() != "transparent"
        )
    }

    /**
     * Returns true if both event start and end contains just date portion.
     *
     * @param event Google Event
     * @return true if all day event, otherwise fall
     */
    static Boolean isAllDayEvent(Event event) {
        assert event

        return event.getStart().getDate() && event.getEnd().getDate()
    }

    static List<ExchangeEvent.Attendee> toExchangeAttendeeList(AttendeeCollection attendeeCollection) {
        assert attendeeCollection

        return attendeeCollection.collect {
            new ExchangeEvent.Attendee(
                    address: it.address,
                    name: it.name,
                    response: it.responseType.name()
            )
        }
    }

    static CalSyncEvent.Attendee toCalSyncAttendee(ExchangeEvent.Attendee attendee, Boolean optional) {
        assert attendee

        def response = CalSyncEvent.Attendee.Response.NO_RESPONSE
        if (attendee.response == MeetingResponseType.Accept.name()) {
            response = CalSyncEvent.Attendee.Response.ACCEPTED
        } else if (attendee.response == MeetingResponseType.Decline.name()) {
            response = CalSyncEvent.Attendee.Response.DECLINED
        } else if (attendee.response == MeetingResponseType.Tentative.name()) {
            response = CalSyncEvent.Attendee.Response.TENTATIVE
        }

        new CalSyncEvent.Attendee(
                address: attendee.address.toLowerCase(), // address might be lowercased by Google
                name: attendee.name,
                response: response,
                isOptional: optional
        )
    }

    /**
     * Maps Exchange Event to CalSyncEvent.
     *
     * @param exchangeEvent Exchange Event
     * @param includeEventBody Whether to include event body or not
     * @return CalSyncEvent
     */
    static CalSyncEvent toCalSyncEvent(ExchangeEvent exchangeEvent, Boolean includeEventBody, Boolean includeAttendees) {
        assert exchangeEvent
        assert includeEventBody != null
        assert includeAttendees != null

        def attendees = includeAttendees ? exchangeEvent.requiredAttendees.collect {
            toCalSyncAttendee(it, false)
        } + exchangeEvent.optionalAttendees.collect {
            toCalSyncAttendee(it, true)
        }.sort { it.address } : [] // sort because Google might return attendees in a different order

        return new CalSyncEvent(
                startDateTime: exchangeEvent.startDateTime,
                endDateTime: exchangeEvent.endDateTime,
                subject: exchangeEvent.subject,
                location: exchangeEvent.location,
                reminderMinutesBeforeStart: exchangeEvent.isReminderSet ? exchangeEvent.reminderMinutesBeforeStart : null,
                body: includeEventBody ? exchangeEvent.body : null,
                isAllDayEvent: exchangeEvent.isAllDayEvent,
                attendees: attendees,
                organizerAddress: exchangeEvent.organizerAddress.toLowerCase(), // address might be lowercased by Google
                organizerName: exchangeEvent.organizerName,
                isBusy: exchangeEvent.isBusy
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

        def attendees = calSyncEvent.attendees.collect {
            def isOrganizer = it.address == calSyncEvent.organizerAddress

            def response = "needsAction"
            if (it.response == CalSyncEvent.Attendee.Response.ACCEPTED) {
                response = "accepted"
            } else if (it.response == CalSyncEvent.Attendee.Response.DECLINED) {
                response = "declined"
            } else if (it.response == CalSyncEvent.Attendee.Response.TENTATIVE) {
                response = "tentative"
            }

            def name = it.name
            if (isOrganizer) {
                name = "$name (Organizer)"
            }

            new EventAttendee(
                    email: it.address,
                    displayName: name,
                    responseStatus: response,
                    optional: it.isOptional,
                    organizer: isOrganizer
            )
        }

        return new Event(
                id: calSyncEvent.googleEventId,
                start: toGoogleEventDateTime(calSyncEvent.isAllDayEvent, calSyncEvent.startDateTime),
                end: toGoogleEventDateTime(calSyncEvent.isAllDayEvent, calSyncEvent.endDateTime),
                summary: calSyncEvent.subject,
                location: calSyncEvent.location,
                reminders: reminders,
                description: calSyncEvent.body,
                attendees: attendees,
                transparency: calSyncEvent.isBusy ? "opaque" : "transparent",
                organizer: new Event.Organizer(
                        email: calSyncEvent.organizerAddress,
                        displayName: calSyncEvent.organizerName
                )
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
                isReminderSet: appointment.isReminderSet,
                reminderMinutesBeforeStart: appointment.reminderMinutesBeforeStart,
                body: toPlainText(MessageBody.getStringFromMessageBody(appointment.body)),
                isCanceled: appointment.isCancelled,
                isAllDayEvent: appointment.isAllDayEvent,
                optionalAttendees: toExchangeAttendeeList(appointment.optionalAttendees),
                requiredAttendees: toExchangeAttendeeList(appointment.requiredAttendees),
                organizerAddress: appointment.organizer.address,
                organizerName: appointment.organizer.name,
                isBusy: appointment.legacyFreeBusyStatus == LegacyFreeBusyStatus.Busy
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