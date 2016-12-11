package com.github.choonchernlim.calsync.core

import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.inject.Inject
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Google Calendar client class.
 */
class GoogleService {
    private static Logger LOGGER = LoggerFactory.getLogger(GoogleService)

    /**
     * Connected client.
     */
    final com.google.api.services.calendar.Calendar client

    /**
     * Batch request to reduce HTTP overhead.
     */
    BatchRequest batch

    @Inject
    GoogleService(GoogleClient googleClient) {
        this.client = googleClient.client
    }

    /**
     * Returns all calendars found.
     *
     * @return All calendars
     */
    private List<CalendarListEntry> getCalendars() {
        LOGGER.info("Retrieving all calendars...")

        List<CalendarListEntry> calendarListEntries = client.calendarList().list().execute().getItems() ?: []

        List<String> calendars = calendarListEntries.collect { it.getSummary() }
        LOGGER.info("\tTotal calendars found: ${calendarListEntries.size()}... [calendars: ${calendars}]")

        return calendarListEntries
    }

    /**
     * Returns ID of the existing calendar or newly created calendar if not found.
     *
     * @param calendarName Calendar name
     * @return Calendar ID
     */
    String getCalendarId(String calendarName) {
        assert calendarName?.trim()

        LOGGER.info("Attempting to find calendar [${calendarName}]...")

        String calendarId = getCalendars().find { it.getSummary() == calendarName }?.getId()

        if (calendarId) {
            LOGGER.info("\tReturning existing calendar [${calendarName}]...")

            return calendarId
        }
        else {
            LOGGER.info("\tCreating new calendar [${calendarName}]...")

            return client.calendars().insert(new Calendar(summary: calendarName)).execute().getId()
        }
    }

    /**
     * Creates a new batch.
     *
     * @return Same instance
     */
    GoogleService createBatch() {
        LOGGER.info("Creating new batch...")

        batch = client.batch()

        return this
    }

    /**
     * Executes batch.
     */
    void executeBatch() {
        assert batch != null

        if (batch.size() > 0) {
            LOGGER.info("Executing batch...")

            batch.execute()
        }
    }

    /**
     * Adds a list of events to be created into existing batch.
     *
     * @param calendarId Calendar ID
     * @param events Events to be created
     * @return Same instance
     */
    GoogleService batchNewEvents(String calendarId, List<CalSyncEvent> events) {
        assert calendarId?.trim()
        assert events != null
        assert batch != null

        LOGGER.info("Total events to be created: ${events.size()}...")

        if (events.isEmpty()) {
            return this
        }

        events.each {
            client.events().insert(calendarId, Mapper.toGoogleEvent(it)).queue(batch, [
                    onSuccess: { Event event, HttpHeaders httpHeaders ->
                        LOGGER.info("\tEvent created: [event: ${event}]")
                    },
                    onFailure: { GoogleJsonError error, HttpHeaders httpHeaders ->
                        LOGGER.error("\tError when creating event: [event: ${it}] [error: ${error.getMessage()}]")
                    }
            ] as JsonBatchCallback<Event>)
        }

        return this
    }

    /**
     * Adds a list of events to be deleted from the calendar into existing batch.
     *
     * @param calendarId Calendar ID
     * @param events Events to be deleted
     * @return Same instance
     */
    GoogleService batchDeletedEvents(String calendarId, List<CalSyncEvent> events) {
        assert calendarId?.trim()
        assert events != null
        assert batch != null

        LOGGER.info("Total events to be deleted: ${events.size()}...")

        if (events.isEmpty()) {
            return this
        }

        events.each {
            client.events().delete(calendarId, it.getGoogleEventId()).queue(batch, [
                    onSuccess: { Void content, HttpHeaders httpHeaders ->
                        LOGGER.info("\tEvent deleted: [event: ${it}]")
                    },
                    onFailure: { GoogleJsonError error, HttpHeaders httpHeaders ->
                        LOGGER.error("\tError when deleting event: [event: ${it}] [error: ${error.getMessage()}]")
                    }
            ] as JsonBatchCallback<Void>)
        }

        return this
    }

    /**
     * Returns calendar events from given date range.
     *
     * @param calendarId Calendar ID
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @return Events if found, otherwise empty list
     */
    List<CalSyncEvent> getEvents(String calendarId, DateTime startDateTime, DateTime endDateTime) {
        assert calendarId?.trim()
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime

        LOGGER.info("Retrieving events from ${startDateTime} to ${endDateTime}...")

        // The default max result is 250. However, if the query results more than that, nothing gets returned.
        // Thus, set max result to 2500, which is the largest value allowed.
        List<CalSyncEvent> events = client.events().
                list(calendarId).
                setMaxResults(2500).
                setTimeMin(Mapper.toGoogleDateTime(startDateTime)).
                setTimeMax(Mapper.toGoogleDateTime(endDateTime)).
                execute().
                getItems()?.collect { Mapper.toCalSyncEvent(it) } ?: []

        LOGGER.info("\tTotal events found: ${events.size()}...")

        return events
    }

    /**
     * Deletes calendar.
     *
     * @param calendarID Calendar ID
     */
    void deleteCalendar(String calendarId) {
        assert calendarId?.trim()

        LOGGER.info("Deleting calendar...")

        client.calendars().delete(calendarId).execute()
    }
}