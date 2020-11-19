# CalSync [![Build Status](https://travis-ci.org/choonchernlim/calsync.svg?branch=master)](https://travis-ci.org/choonchernlim/calsync)

Syncs events from Microsoft Exchange calendar to Google calendar.

## Prerequisites

* Microsoft Exchange account
* Google account
* Java 1.8

## Getting Started

* [Download latest calsync.jar](https://github.com/choonchernlim/calsync/releases).

* Enable Google Calendar API (steps provided by Google):-
    * Use [this wizard](https://console.developers.google.com/start/api?id=calendar) to create or select a project in the Google Developers Console and automatically turn on the API. Click **Continue**, then **Go to credentials**.
    * On the **Add credentials to your project** page, click **Cancel**.
    * Select the **OAuth consent screen** tab. 
        * Click **EDIT APP** link.
        * Specify **Application name** (if missing).
        * Select an **Email address**. 
        * Click **Save**.
    * Select the **Credentials** tab.
        * Click the **Create credentials**
        * Select **OAuth client ID**.
        * Application type: **Desktop app**
        * Name: **Google Calendar API**
        * Click **Create**.
    * Click **Download** icon to the right of the client ID.
    * Rename downloaded JSON file as `client_secret.json`.

* Place `client_secret.json` beside `calsync.jar`.
    
* Run `java -jar calsync.jar` once to create `calsync.conf`.

* Your working directory should now have the following files:-

```
calsync/
   ├──  calsync.conf        <- Generated by `calsync.jar` when running it for the first time.
   ├──  calsync.jar         <- Downloaded from this site
   └──  client_secret.json  <- Downloaded from Google Developers Console
```

* Edit `calsync.conf`.
  
* Run `java -jar calsync.jar` again.


## calsync.conf

If `calsync.conf` is missing (ie: you are running it for the first time), the configuration file will be 
generated for you.

```properties
# Environment variable name containing Exchange user name value.
# If you are using Office 365, the user name value should be your email address.
#
# Accepted value: string.
exchange.username.env=CALSYNC_EXCHANGE_USERNAME

# Environment variable name containing Exchange password value.
#
# Accepted value: string.
exchange.password.env=CALSYNC_EXCHANGE_PASSWORD

# Exchange web service URL.
# If you are using Office 365, the URL should be https://outlook.office365.com/ews/exchange.asmx
#
# Accepted value: string.
exchange.url=https://[EXCHANGE_SERVER]/ews/exchange.asmx

# Sleep on Exchange connection error.
#
# When set to `false`, an exception is thrown when failing to connect against Exchange server.
#
# When set to `true`, CalSync will swallow the thrown connection exception and re-attempt
# on next sync if `next.sync.in.minutes` is greater than 0. This is useful if you are only
# able to connect against Exchange server within work firewall.
#
# Ensure `exchange.url` is set correctly first before enabling this feature so that you
# are able to connect against Exchange server.
#
# Accepted value: true, false.
exchange.sleep.on.connection.error=false

# File path to Google client_secret.json.
#
# Accepted value: string.
google.client.secret.json.file.path=client_secret.json

# A new Google calendar name that DOESN'T MATCH any existing Google calendar names.
# Because CalSync performs one-way sync from Exchange calendar to Google calendar, it will wipe
# any existing events in Google calendar if they don't match events from Exchange calendar.
#
# Accepted value: string.
google.calendar.name=Outlook

# Total days to sync events from current day.
#
# Accepted value: integer greater than 0.
total.sync.in.days=7

# Total days to sync past events from current day, or 0 to not sync past events.
#
# Accepted value: integer greater than or equal to 0.
total.sync.in.days.past=7

# Next sync in minutes, or 0 to disable next run.
#
# Accepted value: integer.
next.sync.in.minutes=15

# Whether to include events marked as "canceled" or not.
#
# Accepted value: true, false.
include.canceled.events=false

# Whether to include event body or not. When syncing from work Exchange calendar, sometimes it's
# safer NOT to copy the event body, which may include sensitive information, or due to work policy.
#
# Accepted value: true, false.
include.event.body=false
```
