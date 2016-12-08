package com.github.choonchernlim.calsync.core

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*

class GoogleCalendarService {

    /**
     * Directory to store user credentials.
     */
    private static final File DATA_STORE_DIR = new File(System.getProperty('user.home'), ".${Constant.PROJECT_ID}")

    private final com.google.api.services.calendar.Calendar client

    // TODO hardcode it here for now
    private UserConfig userConfig = new UserConfig()

    GoogleCalendarService(String clientSecretJsonFilePath) {
        client = configure(clientSecretJsonFilePath)
    }

    /**
     * Authorizes the installed application to access user's protected data.
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
            System.out.println('Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar ' +
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

    void showCalendars() throws IOException {
        View.header('Show Calendars')
        CalendarList feed = client.calendarList().list().execute()
        View.display(feed)
    }

    Calendar getCalendar() throws IOException {
        View.header("Attempting to find calendar [ ${userConfig.calendarName} ] ...")

        String calendarId = client.calendarList().
                list().
                execute().
                getItems().
                find { it.getSummary() == userConfig.calendarName }?.
                getId()

        return calendarId ?
                getCalendarById(calendarId) :
                createCalendar()
    }

    private Calendar getCalendarById(String calendarId) {
        View.header("Get calendar by id [ ${calendarId} ]...")
        return client.calendars().get(calendarId).execute()
    }

    private Calendar createCalendar() {
        View.header('Creating new calendar...')

        Calendar result = client.calendars().insert(new Calendar(summary: userConfig.calendarName)).execute()

        View.display(result)

        return result
    }

//    Calendar addCalendar() throws IOException {
//        View.header('Add Calendar')
//        Calendar entry = new Calendar()
//        entry.setSummary('Calendar for Testing 3')
//        Calendar result = client.calendars().insert(entry).execute()
//        View.display(result)
//        return result
//    }

//    Calendar updateCalendar(Calendar calendar) throws IOException {
//        View.header('Update Calendar')
//        Calendar entry = new Calendar()
//        entry.setSummary('Updated Calendar for Testing')
//        Calendar result = client.calendars().patch(calendar.getId(), entry).execute()
//        View.display(result)
//        return result
//    }


    void addEvent(Calendar calendar) throws IOException {
        View.header('Add Event')
        Event event = newEvent()
        Event result = client.events().insert(calendar.getId(), event).execute()
        View.display(result)
    }

    private static Event newEvent() {
        Event event = new Event()
        event.setSummary('New Event')
        Date startDate = new Date()
        Date endDate = new Date(startDate.getTime() + 3600000)
        DateTime start = new DateTime(startDate, TimeZone.getTimeZone('UTC'))
        event.setStart(new EventDateTime().setDateTime(start))
        DateTime end = new DateTime(endDate, TimeZone.getTimeZone('UTC'))
        event.setEnd(new EventDateTime().setDateTime(end))
        return event
    }

    void showEvents(Calendar calendar) throws IOException {
        View.header('Show Events')
        Events feed = client.events().list(calendar.getId()).execute()
        View.display(feed)
    }


    void deleteCalendar(Calendar calendar) throws IOException {
        View.header('Delete Calendar')
        client.calendars().delete(calendar.getId()).execute()
    }

//    void addCalendarsUsingBatch() throws IOException {
//        View.header('Add Calendars using Batch')
//        BatchRequest batch = client.batch()
//
//        // Create the callback.
//        JsonBatchCallback<Calendar> callback = new JsonBatchCallback<Calendar>() {
//
//            @Override
//            void onSuccess(Calendar calendar, HttpHeaders responseHeaders) {
//                View.display(calendar)
//                addedCalendarsUsingBatch.add(calendar)
//            }
//
//            @Override
//            void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
//                System.out.println('Error Message: ' + e.getMessage())
//            }
//        }
//
//        // Create 2 Calendar Entries to insert.
//        Calendar entry1 = new Calendar().setSummary('Calendar for Testing 1')
//        client.calendars().insert(entry1).queue(batch, callback)
//
//        Calendar entry2 = new Calendar().setSummary('Calendar for Testing 2')
//        client.calendars().insert(entry2).queue(batch, callback)
//
//        batch.execute()
//    }
//
//    void deleteCalendarsUsingBatch() throws IOException {
//        View.header('Delete Calendars Using Batch')
//        BatchRequest batch = client.batch()
//        for (Calendar calendar : addedCalendarsUsingBatch) {
//            client.calendars().delete(calendar.getId()).queue(batch, new JsonBatchCallback<Void>() {
//
//                @Override
//                void onSuccess(Void content, HttpHeaders responseHeaders) {
//                    System.out.println('Delete is successful!')
//                }
//
//                @Override
//                void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
//                    System.out.println('Error Message: ' + e.getMessage())
//                }
//            })
//        }
//
//        batch.execute()
//    }


}