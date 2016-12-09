package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.googlecalendar.GoogleCalendarService
import com.github.choonchernlim.calsync.googlecalendar.MapperUtils
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

        String calendarId = service.getCalendarId(googleCalendarName)

        service.createEvents(calendarId, [
                MapperUtils.toCalSyncEvent(
                        new DateTime(2016, 12, 7, 8, 0, 0),
                        new DateTime(2016, 12, 7, 9, 0, 0),
                        'Subject3',
                        'Location3'),
                MapperUtils.toCalSyncEvent(
                        new DateTime(2016, 12, 11, 8, 0, 0),
                        new DateTime(2016, 12, 12, 9, 0, 0),
                        'Subject4 from 2010 to 2020',
                        'Location4')
        ])

        List<CalSyncEvent> events = service.getEvents(
                calendarId,
                new DateTime(2016, 12, 7, 0, 0, 0),
                new DateTime(2016, 12, 12, 23, 59, 59))

        service.deleteEvents(calendarId, events)

        service.getEvents(
                calendarId,
                new DateTime(2016, 12, 7, 0, 0, 0),
                new DateTime(2016, 12, 12, 23, 59, 59))

        service.deleteCalendar(calendarId)
    }
}