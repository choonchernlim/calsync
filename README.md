# CalSync [![Build Status](https://travis-ci.org/choonchernlim/calsync.svg?branch=master)](https://travis-ci.org/choonchernlim/calsync)

Syncs events from Exchange calendar to Google calendar.

## Getting Started

* Prerequisites:-
    * Java 1.7
    * Maven
    
* Enable Google Calendar API (steps provided by Google):-
    * Use [this wizard](https://console.developers.google.com/start/api?id=calendar) to create or select a project in the Google Developers Console and automatically turn on the API. Click **Continue**, then **Go to credentials**.
    * On the **Add credentials to your project** page, click the **Cancel** button.
    * At the top of the page, select the **OAuth consent screen** tab. Select an **Email address**, enter a **Product name** if not already set, and click the **Save** button.
    * Select the **Credentials** tab, click the **Create credentials** button and select **OAuth client ID**.
    * Select the application type **Other**, enter the name "Google Calendar API Quickstart", and click the **Create** button.
    * Click **OK** to dismiss the resulting dialog.
    * Click the "Download JSON" button to the right of the client ID.
    * Rename it `client_secret.json`.
    
* Create environment variables defined in "Environment Variables" section below.
    
* Download CalSync source code to `/path/to/calsync`.

* Navigate to `/path/to/calsync`.

* Run `mvn clean package`. This will create `calsync.jar` under `/path/to/calsync/target/` directory.

* Run `java -jar calsync.jar`.

## Environment Variables

|Name                                         | Description                                                               |
|---------------------------------------------|---------------------------------------------------------------------------|
| CALSYNC_EXCHANGE_USERNAME                   | Exchange user name.                                                       |
| CALSYNC_EXCHANGE_PASSWORD                   | Exchange password.                                                        |
| CALSYNC_EXCHANGE_URL                        | Exchange web service URL, ex: `https://institution.com/ews/exchange.asmx`.|
| CALSYNC_GOOGLE_CALENDAR_NAME                | One-word calendar name. If the name matches your existing Google calendars, it will use that. Otherwise, a new calendar will be created. |
| CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH | Path to the downloaded `client_secret.json`.                              |
| CALSYNC_TOTAL_SYNC_DAYS                     | Total days to sync from current day.                                      |

### Windows

* Press `Windows` + `Break`
* `Advanced system settings` → `Environment Variables`.

### Mac

* Create or open `~/Library/LaunchAgents/environment.plist`:-

* Enter the following environment variables:-

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>my.startup</string>
  <key>ProgramArguments</key>
  <array>
    <string>sh</string>
    <string>-c</string>
    <string>
	launchctl setenv CALSYNC_EXCHANGE_USERNAME [YOUR_VALUE]
	launchctl setenv CALSYNC_EXCHANGE_PASSWORD [YOUR_VALUE]
	launchctl setenv CALSYNC_EXCHANGE_URL [YOUR_VALUE]
	launchctl setenv CALSYNC_GOOGLE_CALENDAR_NAME [YOUR_VALUE]
	launchctl setenv CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH [YOUR_VALUE]
	launchctl setenv CALSYNC_TOTAL_SYNC_DAYS [YOUR_VALUE]
    </string>
  </array>
  <key>RunAtLoad</key>
  <true/>
</dict>
</plist>
```

* Log out and log back in to load the environment variables.