package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.googlecalendar.GoogleCalendarService
import com.github.choonchernlim.calsync.googlecalendar.View
import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.Event
import org.joda.time.DateTime

class Main {

    static void main(String[] args) {
        if (!args) {
            System.err.println('ERROR: Please specify /file/path/to/client_secret.json on first argument.')
            System.exit(1)
        }

        String clientSecretJsonFilePath = args[0]
        String googleCalendarName = 'Outlook'

        GoogleCalendarService service = new GoogleCalendarService(clientSecretJsonFilePath)

        service.showCalendars()

        Calendar calendar = service.getCalendar(googleCalendarName)

        Event event1 = service.newEvent(
                new DateTime(2016, 12, 7, 8, 0, 0),
                new DateTime(2016, 12, 7, 9, 0, 0),
                'Subject3',
                'Location3')

        Event event2 = service.newEvent(
                new DateTime(2016, 12, 11, 8, 0, 0),
                new DateTime(2016, 12, 12, 9, 0, 0),
                'Subject4 from 2010 to 2020',
                'Location4')

        service.addEvents(calendar, [event1, event2])

        List<Event> events = service.getEvents(
                calendar,
                new DateTime(2016, 12, 7, 0, 0, 0),
                new DateTime(2016, 12, 12, 23, 59, 59))

        events.each {
            View.display(it)
        }

        service.deleteEvents(calendar, events)

        events = service.getEvents(
                calendar,
                new DateTime(2016, 12, 7, 0, 0, 0),
                new DateTime(2016, 12, 12, 23, 59, 59))

        events.each {
            View.display(it)
        }

        service.deleteCalendar(calendar)
    }
}