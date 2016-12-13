package com.github.choonchernlim.calsync.google

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.github.choonchernlim.calsync.core.Mapper
import com.github.choonchernlim.calsync.core.UserConfig
import com.google.api.services.calendar.model.CalendarListEntry
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
     * Connected googleClient.
     */
    final GoogleClient googleClient

    /**
     * Event actions to be executed as one big batch.
     */
    final List<EventAction> eventActions

    @Inject
    GoogleService(GoogleClient googleClient) {
        this.googleClient = googleClient
        this.eventActions = []
    }

    void init(UserConfig userConfig) {
        assert userConfig

        googleClient.init(userConfig)
    }

    /**
     * Returns all calendars found.
     *
     * @return All calendars
     */
    private List<CalendarListEntry> getCalendars() {
        LOGGER.info("Retrieving all calendars...")

        List<CalendarListEntry> calendarListEntries = googleClient.getCalendarList().getItems() ?: []

        List<String> calendars = calendarListEntries.collect { it.getSummary() }
        LOGGER.info("\tTotal calendars found: ${calendarListEntries.size()}... ${calendars}")

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

            return googleClient.createCalendar().getId()
        }
    }

    /**
     * Creates a new batch.
     *
     * @return Same instance
     */
    GoogleService createBatch() {
        LOGGER.info("Creating a new batch...")

        eventActions.clear()

        return this
    }

    /**
     * Executes batch.
     *
     * @param calendarId Calendar ID
     */
    void executeBatch(String calendarId) {
        assert calendarId?.trim()

        if (!eventActions.isEmpty()) {
            LOGGER.info("Executing batch...")

            googleClient.executeActions(calendarId, eventActions)
        }
    }

    /**
     * Adds a list of events to be created into existing batch.
     *
     * @param events Events to be created
     * @return Same instance
     */
    GoogleService batchNewEvents(List<CalSyncEvent> events) {
        assert events != null

        LOGGER.info("Total events to be created: ${events.size()}...")

        if (events.isEmpty()) {
            return this
        }

        eventActions.addAll(events.collect { new EventAction(action: EventAction.Action.INSERT, event: it) })

        return this
    }

    /**
     * Adds a list of events to be deleted from the calendar into existing batch.
     *
     * @param events Events to be deleted
     * @return Same instance
     */
    GoogleService batchDeletedEvents(List<CalSyncEvent> events) {
        assert events != null

        LOGGER.info("Total events to be deleted: ${events.size()}...")

        if (events.isEmpty()) {
            return this
        }

        eventActions.addAll(events.collect { new EventAction(action: EventAction.Action.DELETE, event: it) })

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
        assert startDateTime && endDateTime && startDateTime <= endDateTime

        LOGGER.info(
                "Retrieving events from ${Mapper.humanReadableDateTime(startDateTime)} to ${Mapper.humanReadableDateTime(endDateTime)}...")

        List<CalSyncEvent> events = googleClient.
                getEvents(calendarId, startDateTime, endDateTime).
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

        googleClient.deleteCalendar(calendarId)
    }
}