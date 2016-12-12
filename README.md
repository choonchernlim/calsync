# CalSync [![Build Status](https://travis-ci.org/choonchernlim/calsync.svg?branch=master)](https://travis-ci.org/choonchernlim/calsync)

Syncs events from Exchange calendar to Google calendar.

## Prerequisites

* Exchange account
* Google account
* Java 1.7

## Getting Started

* [Download latest calsync.jar](https://github.com/choonchernlim/calsync/releases).

* Enable Google Calendar API (steps provided by Google):-
    * Use [this wizard](https://console.developers.google.com/start/api?id=calendar) to create or select a project in the Google Developers Console and automatically turn on the API. Click **Continue**, then **Go to credentials**.
    * On the **Add credentials to your project** page, click the **Cancel** button.
    * At the top of the page, select the **OAuth consent screen** tab. Select an **Email address**, enter a **Product name** if not already set, and click the **Save** button.
    * Select the **Credentials** tab, click the **Create credentials** button and select **OAuth client ID**.
    * Select the application type **Other**, enter the name "Google Calendar API Quickstart", and click the **Create** button.
    * Click **OK** to dismiss the resulting dialog.
    * Click the "Download JSON" button to the right of the client ID.
    * Rename it `client_secret.json`.
    * Place it besides the downloaded `calsync.jar`.
    
* Run `java -jar calsync.jar` once to create `calsync.conf`.

* Your working directory should now have the following files:-

```
calsync/
   ├──  calsync.conf
   ├──  calsync.jar
   └──  client_secret.json
```

* Edit `calsync.conf`.
  
* Run `java -jar calsync.jar` again.
