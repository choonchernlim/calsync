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
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.inject.Inject
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
    private static final File DATA_STORE_DIR = new File(System.getProperty('user.home'), ".${Constant.PROJECT_ID}")

    final UserConfig userConfig

    // final Calendar client

    @Inject
    GoogleClient(UserConfig userConfig) {
        this.userConfig = userConfig
        // this.client = getClient()
    }

    /**
     * Performs app authorization and returns connected client.
     *
     * @return Connected client
     */
    Calendar getClient() {
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

        return new Calendar.Builder(httpTransport, jsonFactory, credential).
                setApplicationName(Constant.PROJECT_ID).
                build()
    }
//
//    CalendarList getCalendarList() {
//        return client.calendarList().list().execute()
//    }
}