# AndroidTwitterApp
This app is an android app that allows a Twitter user login to Twitter, view their Twitter timeline, post a new tweet, as well as retweet/reply/favorite/unfavorite tweets. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: 50 hours spent in total

## User Stories

The following **required** functionality is completed:

* [x]	User can **sign in to Twitter** using OAuth login
* [x]	User can **view tweets from their home timeline**
  * [x] User is displayed the username, name, and body for each tweet
  * [x] User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
  * [x] User can view more tweets as they scroll with [infinite pagination](http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews). Number of tweets is unlimited.
    However there are [Twitter Api Rate Limits](https://dev.twitter.com/rest/public/rate-limiting) in place.
* [x] User can **compose and post a new tweet**
  * [x] User can click a “Compose” icon in the Action Bar on the top right
  * [x] User can then enter a new tweet and post this to twitter
  * [x] User is taken back to home timeline with **new tweet visible** in timeline

The following **optional** features are implemented:

* [x] User can **see a counter with total number of characters left for tweet** on compose tweet page
* [x] User can **click a link within a tweet body** on tweet details view. The click will launch the web browser with relevant page opened.
* [x] User can **pull down to refresh tweets timeline**
* [x] User can **open the twitter app offline and see last loaded tweets**. Persisted in SQLite tweets are refreshed on every application launch. While "live data" is displayed when app can get it from Twitter API, it is also saved for use in offline mode.
* [x] User can tap a tweet to **open a detailed tweet view**
* [x] User can **select "reply" from detail view to respond to a tweet**
* [x] Improve the user interface and theme the app to feel "twitter branded"

The following **bonus** features are implemented:

* [x] User can see embedded image media within the tweet detail view
* [x] Compose tweet functionality is build using modal overlay

The following **additional** features are implemented:

* [x] Use of ViewHolder pattern and Parcelable to improve app performance
* [x] Organizing the code through packages and externalizing all string resources into strings.xml
* [x] Robust error handling, check if internet is available, handle error cases, network failures. Show Toast messages in case of mentioned error cases.
* [x] User can see **retweet count** and **favorites count** for each tweet on timeline page and details page.
* [x] User can see an indicator text to distinguish if the tweet is a **retweet** or a **reply** for each tweet on timeline.
* [x] User can **select "reply" from timeline to respond to a tweet** which takes user to a dialog fragment. Upon posting the reply, user is taken back to home timeline with **new reply tweet visible** in timeline
* [x] User can **select "retweet" from detail view or timeline** to retweet a tweet which would result in retweet icon color to turn from grey to green and retweet count to increment in both details page and timeline page for that tweet.
* [x] User can **select "favorite" from detail view or timeline** to favorite a tweet which would result in favorite icon color to turn from grey to yellow and favorites count to increment in both details page and timeline page for that tweet.
* [x] User can **select "unfavorite" from detail view or timeline** to unfavorite a tweet which would result in favorite icon color to turn from yellow to grey and favorites count to decrement in both details page and timeline page for that tweet.
* [x] User can **select "un-retweet" from detail view or timeline** to undo a retweet which would result in retweet icon color to turn from green to grey and retweet count to decrement in both details page and timeline page for that tweet.
* [x] Vertically **scrollable** detail view. 

## Video Walkthrough 

I have two walkthroughs of implemented user stories. The difference is that the first walkthrough has the Compose Activity that has a more Twitter look, where the second one has Compose DialogFragmnet instead of Activity but it does not look like Twitter's own Compose Fragment.

Walkthrough 1

<img src='https://github.com/esrako/AndroidTwitterApp/blob/master/project3_01.gif' title='Video Walkthrough 1' width='' alt='Video Walkthrough' />

Walkthrough 2

<img src='https://github.com/esrako/AndroidTwitterApp/blob/master/project3_02.gif' title='Video Walkthrough 1' width='' alt='Video Walkthrough' />

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

These are some of the design decisions taken during this project, some inspired by the project hints given where some are independent decisions.

* Regarding compose a new tweet: When a tweet is composed, it is shown in timeline immediately, in the mean time, the page is refreshed. Please note that composed tweet would be visible before page is refreshed.
* Regarding favorite/unfavorite/retweet: Retweet and favorites counts, green/grey retweet icons, yellow/grey favorite icons are updated for the original tweet once the action is done, but full page refresh is not forced.
* Regarding reply, once replied to a tweet, new tweet(reply tweet) will be added at the top of the timeline page but page is not forced to be refreshed.

## Open-source libraries used

- [RestClient Template](https://github.com/codepath/android-rest-client-template) - Android OAuth REST Client 
- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Picasso](http://square.github.io/picasso/) - Image loading and caching library for Android

## License

    Copyright [2015] [Esra Kucukoguz]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
