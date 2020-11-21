package com.github.choonchernlim.calsync.core

import groovy.transform.PackageScope

/**
 * Class to read user config or create a default config file if not exist.
 */
class UserConfigReader {

    static final String SAMPLE_CONF = 'calsync-sample.conf'

    static final String EXCHANGE_USERNAME_ENV_KEY = 'exchange.username.env'
    static final String EXCHANGE_PASSWORD_ENV_KEY = 'exchange.password.env'
    static final String EXCHANGE_URL_KEY = 'exchange.url'
    static final String EXCHANGE_SLEEP_ON_CONNECTION_ERROR = 'exchange.sleep.on.connection.error'
    static final String GOOGLE_CLIENT_SECRET_JSON_KEY = 'google.client.secret.json.file.path'
    static final String GOOGLE_CALENDAR_NAME_KEY = 'google.calendar.name'
    static final String TOTAL_SYNC_IN_DAYS_KEY = 'total.sync.in.days'
    static final String TOTAL_SYNC_IN_DAYS_PAST_KEY = 'total.sync.in.days.past'
    static final String NEXT_SYNC_IN_MINUTES_KEY = 'next.sync.in.minutes'
    static final String INCLUDE_CANCELED_EVENTS_KEY = 'include.canceled.events'
    static final String INCLUDE_EVENT_BODY_KEY = 'include.event.body'

    /**
     * Returns user config.
     *
     * @return User config
     */
    UserConfig getUserConfig() {
        return validate(loadProps())
    }

    /**
     * Loads properties by attempting to read the config file. If file doesn't exist, then
     * create one before throwing exception.
     *
     * @return Properties
     */
    @PackageScope
    @SuppressWarnings("GrMethodMayBeStatic")
    Properties loadProps() {
        Properties props = new Properties()
        File propsFile = new File(Constant.CONFIG_FILE_PATH)

        if (propsFile.exists()) {
            propsFile.withInputStream { props.load(it) }
            return props
        }

        propsFile.write(this.class.classLoader.getResource(SAMPLE_CONF).text)

        throw new CalSyncException(
                "${Constant.CONFIG_FILE_PATH} not found... creating one at ${propsFile.getAbsoluteFile()}. " +
                "Please edit this file, then run it again.")
    }

    /**
     * Validates the properties before creating the user config object.
     *
     * @param props Properties
     * @return User config
     */
    @PackageScope
    @SuppressWarnings("GrMethodMayBeStatic")
    UserConfig validate(Properties props) {
        List<String> errors = []

        String exchangeUserName = validatePropEnv(props, errors, EXCHANGE_USERNAME_ENV_KEY)
        String exchangePassword = validatePropEnv(props, errors, EXCHANGE_PASSWORD_ENV_KEY)
        String exchangeUrl = validatePropString(props, errors, EXCHANGE_URL_KEY)
        Boolean exchangeSleepOnConnectionError = validatePropBoolean(props, errors, EXCHANGE_SLEEP_ON_CONNECTION_ERROR)

        String googleClientSecretJsonFilePath = validatePropString(props, errors, GOOGLE_CLIENT_SECRET_JSON_KEY)
        String googleCalendarName = validatePropString(props, errors, GOOGLE_CALENDAR_NAME_KEY)

        Integer totalSyncDays = validatePropInteger(props, errors, TOTAL_SYNC_IN_DAYS_KEY)

        if (totalSyncDays != null && totalSyncDays <= 0) {
            errors.add("${TOTAL_SYNC_IN_DAYS_KEY}: Must be greater than 0.")
        }

        Integer totalSyncDaysPast = validatePropInteger(props, errors, TOTAL_SYNC_IN_DAYS_PAST_KEY)

        if (totalSyncDaysPast != null && totalSyncDaysPast < 0) {
            errors.add("${TOTAL_SYNC_IN_DAYS_PAST_KEY}: Must be greater than or equal to 0.")
        }

        Integer nextSyncInMinutes = validatePropInteger(props, errors, NEXT_SYNC_IN_MINUTES_KEY)

        Boolean includeCanceledEvents = validatePropBoolean(props, errors, INCLUDE_CANCELED_EVENTS_KEY)
        Boolean includeEventBody = validatePropBoolean(props, errors, INCLUDE_EVENT_BODY_KEY)

        if (!errors.isEmpty()) {
            throw new CalSyncException(
                    "The configuration is invalid. Please fix the errors below, then run it again:-" +
                    "\n- ${errors.join('\n- ')}")
        }

        return new UserConfig(
                exchangeUserName: exchangeUserName,
                exchangePassword: exchangePassword,
                exchangeUrl: exchangeUrl,
                exchangeSleepOnConnectionError: exchangeSleepOnConnectionError,
                googleClientSecretJsonFilePath: googleClientSecretJsonFilePath,
                googleCalendarName: googleCalendarName,
                totalSyncDays: totalSyncDays,
                totalSyncDaysPast: totalSyncDaysPast,
                nextSyncInMinutes: nextSyncInMinutes,
                includeCanceledEvents: includeCanceledEvents,
                includeEventBody: includeEventBody
        )
    }

    /**
     * Ensure environment variable property exist with non-blank value.
     *
     * @param props Properties
     * @param errors Error list
     * @param propKey Property key
     * @return Environment variable value if valid, otherwise null
     */
    private String validatePropEnv(Properties props, List<String> errors, String propKey) {
        String env = validatePropString(props, errors, propKey)

        if (!env) {
            return null
        }

        String value = System.getenv(env)?.trim()

        if (!value) {
            errors.add("${propKey}: Environment variable does not have a value.")
            return null
        }

        return value
    }

    /**
     * Ensures property has integer value.
     *
     * @param props Properties
     * @param errors Error list
     * @param propKey Property key
     * @return Integer value if valid, otherwise null
     */
    private Integer validatePropInteger(Properties props, List<String> errors, String propKey) {
        String value = validatePropString(props, errors, propKey)

        if (!value) {
            return null
        }

        if (!value.isInteger()) {
            errors.add("${propKey}: Must be an integer.")
            return null
        }

        return value.toInteger()
    }

    /**
     * Ensures property has boolean value.
     *
     * @param props Properties
     * @param errors Error list
     * @param propKey Property key
     * @return Boolean value if valid, otherwise null
     */
    private Boolean validatePropBoolean(Properties props, List<String> errors, String propKey) {
        String value = validatePropString(props, errors, propKey)

        if (!value) {
            return null
        }

        if (!value.toLowerCase().matches(/true|false/)) {
            errors.add("${propKey}: Must be true or false.")
            return null
        }

        return Boolean.valueOf(value)
    }

    /**
     * Ensures property has string value.
     *
     * @param props Properties
     * @param errors Error list
     * @param propKey Property key
     * @return String value if valid, otherwise null
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    private String validatePropString(Properties props, List<String> errors, String propKey) {
        String value = props.getProperty(propKey)?.trim()

        if (!value) {
            errors.add("${propKey}: Missing value.")
        }

        return value
    }
}
