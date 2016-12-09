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
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import org.joda.time.DateTime

/**
 * Google Calendar client class.
 */
class GoogleCalendarService {

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

    void showCalendars() {
        View.header('Show Calendars')
        CalendarList feed = client.calendarList().list().execute()
        View.display(feed)
    }

    /**
     * Returns the existing calendar or newly created calendar if not found.
     *
     * @param calendarName Calendar name
     * @return Calendar
     */
    Calendar getCalendar(String calendarName) {
        assert calendarName?.trim()

        View.header("Attempting to find calendar [ ${calendarName} ] ...")

        String calendarId = client.calendarList().list().execute().
                getItems()?.
                find { it.getSummary() == calendarName }?.
                getId()

        if (calendarId) {
            View.header("Get calendar by id [ ${calendarId} ]...")
            return client.calendars().get(calendarId).execute()
        }
        else {
            View.header('Creating new calendar...')
            return client.calendars().insert(new Calendar(summary: calendarName)).execute()
        }
    }

    /**
     * Adds a list of events to the calendar.
     *
     * @param calendar Calendar
     * @param events Events to be added
     */
    void addEvents(Calendar calendar, List<Event> events) {
        assert calendar?.getId()?.trim()
        assert !events?.isEmpty()

        View.header('Adding events')

        BatchRequest batch = client.batch()

        events.each {
            client.events().insert(calendar.getId(), it).queue(batch, [
                    onSuccess: { Event event, HttpHeaders httpHeaders ->
                        View.display(event)
                    },
                    onFailure: { GoogleJsonError googleJsonError, HttpHeaders httpHeaders ->
                        System.out.println("Error Message: ${googleJsonError.getMessage()}")
                    }
            ] as JsonBatchCallback<Event>)
        }

        batch.execute()
    }

    /**
     * Deletes a list of events from calendar.
     *
     * @param calendar Calendar
     * @param events Events to be deleted
     */
    void deleteEvents(Calendar calendar, List<Event> events) {
        assert calendar?.getId()?.trim()
        assert !events?.isEmpty()

        View.header('Deleting Events...')

        BatchRequest batch = client.batch()

        events.each {
            View.display(it)

            client.events().delete(calendar.getId(), it.getId()).queue(batch, [
                    onSuccess: { Void content, HttpHeaders httpHeaders ->
                        View.header("Event is successfully deleted!")
                    },
                    onFailure: { GoogleJsonError googleJsonError, HttpHeaders httpHeaders ->
                        System.out.println("Error Message: ${googleJsonError.getMessage()}")
                    }
            ] as JsonBatchCallback<Void>)
        }

        batch.execute()
    }

    /**
     * Creates new {@link Event} object.
     *
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @param summary Event title
     * @param location Location
     * @return {@link Event} object
     */
    static Event newEvent(DateTime startDateTime, DateTime endDateTime, String summary, String location = null) {
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime
        assert summary?.trim()

        return new Event(
                start: MapperUtils.toGoogleEventDateTime(startDateTime),
                end: MapperUtils.toGoogleEventDateTime(endDateTime),
                summary: summary,
                location: location
        )
    }

    /**
     * Returns calendar events from given date range.
     *
     * @param calendar Calendar
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @return Events if found, otherwise empty list
     */
    List<CalSyncEvent> getEvents(Calendar calendar, DateTime startDateTime, DateTime endDateTime) {
        assert calendar?.getId()?.trim()
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime

        View.header("Getting Events from ${startDateTime} to ${endDateTime}...")

        return client.events().
                list(calendar.getId()).
                setTimeMin(MapperUtils.toGoogleDateTime(startDateTime)).
                setTimeMax(MapperUtils.toGoogleDateTime(endDateTime)).
                execute().
                getItems()?.collect { MapperUtils.toCalSyncEvent(it) } ?: []
    }

    /**
     * Deletes calendar.
     *
     * @param calendar Calendar to be deleted
     */
    void deleteCalendar(Calendar calendar) {
        assert calendar?.getId()?.trim()

        View.header('Delete Calendar')

        client.calendars().delete(calendar.getId()).execute()
    }
}