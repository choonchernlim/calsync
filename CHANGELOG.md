# Change Log

## 0.5.0 - 2019-10-14

* FEATURE - Added respond type (Accepted, Declined, Unresponded, Tentative, Organizer, Unknown) on Google event title.
* BUG - Excluded `reminderMinutesBeforeStart` from CalSyncEvent's equals and hashcode to get `equals` to match properly. This prevents "same" Google/Exchange events from being deleted/recreated again and again.
  
## 0.4.1 - 2017-03-08

* Fixed problem where all-day Exchange event from previous day gets created every time CalSync runs.                      

## 0.4.0 - 2016-12-16

* `exchange.sleep.on.connection.error` = Whether to suppress thrown Exchange connection error or not.

## 0.3.0 - 2016-12-15

* Handled all-day event.
* Added an all-day no-notification Google event `CalSync - Last Sync: MMM DD @ hh:mm a` so that this event is always appear on top of the current day. Similar past events are automatically deleted to prevent clutter.

## 0.2.0 - 2016-12-13

* Two new configuration options:-
    * `include.canceled.events` = Whether to include canceled events or not.
    * `include.event.body` = Whether to include event body or not.

* Better console log messages.

## 0.1.0 - 2016-12-11

* Initial.
