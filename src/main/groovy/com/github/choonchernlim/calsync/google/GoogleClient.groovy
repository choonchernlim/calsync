package com.github.choonchernlim.calsync.google

import com.github.choonchernlim.calsync.core.CalSyncException
import com.github.choonchernlim.calsync.core.Constant
import com.github.choonchernlim.calsync.core.Mapper
import com.github.choonchernlim.calsync.core.UserConfig
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
import com.google.api.services.calendar.model.Events
import com.google.inject.Inject
import groovy.transform.PackageScope
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Google Calendar client class.
 */
@PackageScope
class GoogleClient {
    private static Logger LOGGER = LoggerFactory.getLogger(GoogleClient)

    /**
     * Directory to store user credentials.
     */
    private static final File DATA_STORE_DIR = new File(System.getProperty('user.home'), ".${Constant.PROJECT_ID}")

    final UserConfig userConfig

    final com.google.api.services.calendar.Calendar client

    @Inject
    GoogleClient(UserConfig userConfig) {
        this.userConfig = userConfig
        this.client = getClient()
    }

    /**
     * Performs app authorization and returns connected client.
     *
     * @return Connected client
     */
    com.google.api.services.calendar.Calendar getClient() {
        LOGGER.info('Authenticating against Google...')

        // initialize the transport
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        // initialize the data store factory
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR)

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance()

        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory,
                new InputStreamReader(new FileInputStream(userConfig.googleClientSecretJsonFilePath)))

        if (clientSecrets.getDetails().getClientId().startsWith('Enter')
                || clientSecrets.getDetails().getClientSecret().startsWith('Enter ')) {
            throw new CalSyncException(
                    'Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar into ' +
                    '/path/to/client_secrets.json')
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
     * Returns all calendars.
     *
     * @return Calendars.
     */
    CalendarList getCalendarList() {
        return client.calendarList().list().execute()
    }

    /**
     * Creates a calendar for this app.
     *
     * @return Created calendar
     */
    Calendar createCalendar() {
        return client.calendars().
                insert(new Calendar(summary: userConfig.googleCalendarName)).
                execute()
    }

    /**
     * Returns events based on the given datetime range.
     *
     * @param calendarId Calendar ID
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @return Events
     */
    Events getEvents(String calendarId, DateTime startDateTime, DateTime endDateTime) {
        assert calendarId?.trim()
        assert startDateTime
        assert endDateTime

        // The default max result is 250. However, if the query results more than that, nothing gets returned.
        // Thus, set max result to 2500, which is the largest value allowed.
        return client.events().
                list(calendarId).
                setMaxResults(2500).
                setTimeMin(Mapper.toGoogleDateTime(startDateTime)).
                setTimeMax(Mapper.toGoogleDateTime(endDateTime)).
                execute()
    }

    /**
     * Deletes an existing calendar.
     *
     * @param calendarId Calendar ID
     */
    void deleteCalendar(String calendarId) {
        assert calendarId?.trim()

        client.calendars().delete(calendarId).execute()
    }

    /**
     * Executes all event actions as one big batch.
     *
     * @param calendarId Calendar ID
     * @param eventActions Event actions
     */
    void executeActions(String calendarId, List<EventAction> eventActions) {
        assert calendarId?.trim()
        assert eventActions != null

        if (eventActions.isEmpty()) {
            return
        }

        BatchRequest batch = client.batch()
        com.google.api.services.calendar.Calendar.Events events = client.events()

        // Deletes outdated events first
        eventActions.
                findAll { it.action == EventAction.Action.DELETE }.
                collect { it.event }.
                each { event ->
                    events.delete(calendarId, event.googleEventId).queue(batch, [
                            onSuccess: { Void content, HttpHeaders httpHeaders ->
                                LOGGER.info("\tEvent deleted: [event: ${event}]")
                            },
                            onFailure: { GoogleJsonError error, HttpHeaders httpHeaders ->
                                LOGGER.error("\tError when deleting event: [event: ${event}] [error: ${error}]")
                            }
                    ] as JsonBatchCallback<Void>)
                }

        // Adds new events later
        eventActions.
                findAll { it.action == EventAction.Action.INSERT }.
                collect { it.event }.
                each { event ->
                    events.insert(calendarId, Mapper.toGoogleEvent(event)).
                            queue(batch, [
                                    onSuccess: { Event googleEvent, HttpHeaders httpHeaders ->
                                        LOGGER.info("\tEvent created: [event: ${event}]")
                                    },
                                    onFailure: { GoogleJsonError error, HttpHeaders httpHeaders ->
                                        LOGGER.error(
                                                "\tError when creating event: [event: ${event}] [error: ${error}]")
                                    }
                            ] as JsonBatchCallback<Event>)
                }

        batch.execute()
    }
}