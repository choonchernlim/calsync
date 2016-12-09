package com.github.choonchernlim.calsync.googlecalendar

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.github.choonchernlim.calsync.core.Constant
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
class GoogleCalendarService {
    private static Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarService)

    /**
     * Directory to store user credentials.
     */
    private static final File DATA_STORE_DIR = new File(System.getProperty('user.home'), ".${Constant.PROJECT_ID}")

    private final com.google.api.services.calendar.Calendar client

    GoogleCalendarService(String clientSecretJsonFilePath) {
        client = configure(clientSecretJsonFilePath)
    }

    /**
     * Performs app authorization and returns connected client.
     *
     * @param clientSecretJsonFilePath File path to client_secret.json
     * @return Connected client
     */
    private static com.google.api.services.calendar.Calendar configure(String clientSecretJsonFilePath) {
        LOGGER.info('Performing app authorization...')

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
                setApplicationName(Constant.PROJECT_ID).
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

        LOGGER.info(calendarListEntries.collect { it.getSummary() }?.join("; "))

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
            LOGGER.info("Returning existing calendar [${calendarName}]...")

            return calendarId
        }
        else {
            LOGGER.info("Creating new calendar [${calendarName}]...")

            return client.calendars().insert(new Calendar(summary: calendarName)).execute().getId()
        }
    }

    /**
     * Creates a list of events for the calendar.
     *
     * @param calendarId Calendar ID
     * @param events Events to be added
     */
    void createEvents(String calendarId, List<CalSyncEvent> events) {
        assert calendarId?.trim()
        assert !events?.isEmpty()

        BatchRequest batch = client.batch()

        events.each {
            LOGGER.info("Adding event [${it}]...")

            client.events().insert(calendarId, MapperUtils.toGoogleEvent(it)).queue(batch, [
                    onSuccess: { Event event, HttpHeaders httpHeaders ->
                        LOGGER.info(MapperUtils.toString(event))
                    },
                    onFailure: { GoogleJsonError googleJsonError, HttpHeaders httpHeaders ->
                        LOGGER.error("Error when adding event: ${googleJsonError.getMessage()}")
                    }
            ] as JsonBatchCallback<Event>)
        }

        batch.execute()
    }

    /**
     * Deletes a list of events from calendar.
     *
     * @param calendarId Calendar ID
     * @param events Events to be deleted
     */
    void deleteEvents(String calendarId, List<CalSyncEvent> events) {
        assert calendarId?.trim()
        assert !events?.isEmpty()

        BatchRequest batch = client.batch()

        events.each {
            LOGGER.info("Deleting event [${it}]...")

            client.events().delete(calendarId, it.getGoogleEventId()).queue(batch, [
                    onSuccess: { Void content, HttpHeaders httpHeaders ->
                        LOGGER.info('Event is successfully deleted!')
                    },
                    onFailure: { GoogleJsonError googleJsonError, HttpHeaders httpHeaders ->
                        LOGGER.error("Error when adding event: ${googleJsonError.getMessage()}")
                    }
            ] as JsonBatchCallback<Void>)
        }

        batch.execute()
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

        return client.events().
                list(calendarId).
                setTimeMin(MapperUtils.toGoogleDateTime(startDateTime)).
                setTimeMax(MapperUtils.toGoogleDateTime(endDateTime)).
                execute().
                getItems()?.collect { MapperUtils.toCalSyncEvent(it) } ?: []
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