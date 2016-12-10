package com.github.choonchernlim.calsync.core

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Google Calendar client class.
 */
class GoogleClient {
    private static Logger LOGGER = LoggerFactory.getLogger(GoogleClient)

    /**
     * Directory to store user credentials.
     */
    private static final File DATA_STORE_DIR = new File(System.getProperty('user.home'), ".${AppConfig.PROJECT_ID}")

    /**
     * Connected client.
     */
    com.google.api.services.calendar.Calendar client

    /**
     * Batch request to reduce HTTP overhead.
     */
    BatchRequest batch

    GoogleClient(String clientSecretJsonFilePath) {
        this.client = configure(clientSecretJsonFilePath)
    }

    /**
     * Performs app authorization and returns connected client.
     *
     * @param clientSecretJsonFilePath File path to client_secret.json
     * @return Connected client
     */
    private static com.google.api.services.calendar.Calendar configure(String clientSecretJsonFilePath) {
        LOGGER.info('Authenticating against Google...')

        // initialize the transport
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        // initialize the data store factory
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR)

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance()

        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory,
                new InputStreamReader(new FileInputStream(clientSecretJsonFilePath)))

        if (clientSecrets.getDetails().getClientId().startsWith('Enter')
                || clientSecrets.getDetails().getClientSecret().startsWith('Enter ')) {
            System.err.println('Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar ' +
                               'into /path/to/client_secrets.json')
            System.exit(1)
        }

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, [CalendarScopes.CALENDAR]).
                setDataStoreFactory(dataStoreFactory)
                .build()

        // configure
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize('user')

        return new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential).
                setApplicationName(AppConfig.PROJECT_ID).
                build()
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
    GoogleClient createBatch() {
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
    GoogleClient batchNewEvents(String calendarId, List<CalSyncEvent> events) {
        assert calendarId?.trim()
        assert events != null
        assert batch != null

        LOGGER.info("Total events to be created: ${events.size()}...")

        if (events.isEmpty()) {
            return this
        }

        events.each {
            client.events().insert(calendarId, MapperUtils.toGoogleEvent(it)).queue(batch, [
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
    GoogleClient batchDeletedEvents(String calendarId, List<CalSyncEvent> events) {
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
                setTimeMin(MapperUtils.toGoogleDateTime(startDateTime)).
                setTimeMax(MapperUtils.toGoogleDateTime(endDateTime)).
                execute().
                getItems()?.collect { MapperUtils.toCalSyncEvent(it) } ?: []

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