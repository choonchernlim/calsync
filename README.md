

https://developers.google.com/google-apps/calendar/quickstart/java


# Environment Variables

|Name                                         | Description                                                               |
|---------------------------------------------|---------------------------------------------------------------------------|
| CALSYNC_EXCHANGE_USERNAME                   | Exchange user name.                                                       |
| CALSYNC_EXCHANGE_PASSWORD                   | Exchange password.                                                        |
| CALSYNC_EXCHANGE_URL                        | Exchange web service URL, ex: `https://institution.com/ews/exchange.asmx`.|
| CALSYNC_GOOGLE_CALENDAR_NAME                | One-word calendar name. If the name matches your existing Google calendars, it will use that. Otherwise, a new calendar will be created. |
| CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH | Path to the downloaded `client_secret.json`.                              |
| CALSYNC_TOTAL_SYNC_DAYS                     | Total days to sync from current day.                                      |

## Windows

* Press `Windows` + `Break`
* `Advanced system settings` → `Environment Variables`.

## Mac

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

* Restart machine.