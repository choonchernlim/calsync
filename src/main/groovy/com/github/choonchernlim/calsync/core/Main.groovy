package com.github.choonchernlim.calsync.core

import com.google.api.services.calendar.model.Calendar

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
            service.addEvent(calendar)
            service.showEvents(calendar)
            //service.deleteCalendarsUsingBatch()
            service.deleteCalendar(calendar)
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    }
}