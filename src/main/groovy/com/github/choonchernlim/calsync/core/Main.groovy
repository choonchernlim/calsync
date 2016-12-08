package com.github.choonchernlim.calsync.core

import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.Event
import org.joda.time.DateTime

class Main {

    static void main(String[] args) {
        if (!args) {
            System.err.println('ERROR: Please specify /file/path/to/client_secret.json on first argument.')
            System.exit(1)
        }

        try {
            GoogleCalendarService service = new GoogleCalendarService(args[0])

            service.showCalendars()
            //service.addCalendarsUsingBatch()
            //Calendar calendar = service.addCalendar()

            Calendar calendar = service.getCalendar()

            //service.updateCalendar(calendar)
            Event event = service.newEvent(
                    new DateTime(2016, 12, 8, 8, 0),
                    new DateTime(2016, 12, 8, 9, 0),
                    'Subject3',
                    'Location3')

            // service.addEvent(calendar, event)

            service.addEvents(calendar, event)
            service.showEvents(calendar)
            //service.deleteCalendarsUsingBatch()
            // service.deleteCalendar(calendar)
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    }
}