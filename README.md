# Monetizr Android SDK library

## What is Monetizr?

 Monetizr is a game reward engine, we drive revenue to your business and enhance the experience of your players!
   
 Monetizr rewards your users with an opportunity to unlock and buy your own game merchandise (t-shirts, hats, 3d-figurines, decals, and 40+ other products),
 gift cards (Amazon, Apple, etc.), and even brand sponsored rewards (from brands that fit your core audience and goes well 
 with the narrative of the game). How you want to monetize, you decide!
 
## Monetizr Android SDK

Monetizr Android SDK is a library project built upon Monetizr RESTful API to allow smoother integration with Monetizr

Monetizr SDK is written using singleton pattern (or anti-pattern). The choice was obvious, it allows easier integration and does not require hardcore set-up.

You can find instructions on building the project in [README.md][1] and in integration documentation [Docs][2].

### Setting up

Monetizr SDK is published using jCenter. You can, of course download/clone repozitory and add it as a module dependecy to your project.

Monetizr is providing a per-client based API key`s. We are using oAuth 2 authentication and simplifying that for our client SDKs.
API is a public two-way, that does not expose any useful information, but still be aware of this limitation.
API settings are being provided in-application.

Library can be added to your project in couple of ways.
 
**1. First approach (suggested)**

Use Gradle:

```gradle
repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation 'io.monetizr.monetizrsdk:MonetizrSDK:1.0.0'
}
```

Or Maven:

```xml
<dependency>
	<groupId>io.monetizr.monetizrsdk</groupId>
	<artifactId>MonetizrSDK</artifactId>
	<version>1.0.0</version>
	<type>pom</type>
</dependency>
```
 
**2. Second approach**

 * Open your project in Android Studio

 * Download the library (using Git, or a zip archive to unzip)
   
 * Go to File > Import Module and import the library as a module

 * Right-click your app in project view and select "Open Module Settings"

 * Click the "Dependencies" tab and then the '+' button

 * Select "Module Dependency"

 * Select "Library Project"

 * Modify the library if you want to


**3. Third approach**

 * Open your project in Android Studio

 * Download the library (using Git, or a zip archive to unzip)

 * Create a folder "subProject" in your project

 * Copy and paste the library folder to your subProject folder

 * On the root of your project directory create/modify the settings.gradle file. It should contain something like the following:

```grade
include 'MyApp', ':monetizrsdk'

```
 * gradle clean & build/close the project and reopen/re-import it.

 * Edit your project's build.gradle to add this in the "dependencies" section:

```gradle
dependencies {
//...
    implementation project(':monetizrsdk')
}
```


### Using library inside application

Application needs to have initial settings:

 * API key provided by Monetizr team. Adding the API key: this should be called once, before invoking Monetizr Library for the first time

```kotlin
MonetizrSdk.apikey = "API key provided from Monetizr team"
```

 **Default API key: "4D2E54389EB489966658DDD83E2D1"**

 * Optional. API debugging. Necessary if you wish to see Logging information, by default it is false
 
```kotlin
MonetizrSdk.debuggable = true
```

 * Library defines two permissions, access to internet and necessity to check for internet connection

```xml
 <uses-permission android:name="android.permission.INTERNET"/>
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

* You will need to have your own reward tags, contact Monetizr team to acquire them. 
They are added for each API key, for example, for default api key Monetizr provides

  * monetizr-sample-t-shirt
 
  * 30-credits
 
  * product_1
 
  * product_2
 
  * product_3
 
  * 1-credit


* To invoke reward window, call Monetizr static method from within you application by providing available reward tag
One call will invoke one reward window. Whenever you are ready to show reward window inside your application, you can invoke Monetizr sdk to show reward window


```kotlin
MonetizrSdk.showProductForTag("monetizr-sample-t-shirt")

MonetizrSdk.showProductForTag("30-credits")

MonetizrSdk.showProductForTag("product_1")

MonetizrSdk.showProductForTag("product_2")

MonetizrSdk.showProductForTag("product_3")

MonetizrSdk.showProductForTag("1-credit")

```

### Additional available methods, for helping to improve monetization process and in-game analysis

Common functionality: methods are calling RESTful API and safely storing provided bits of the data using queued HTTPS requests.
If something fails, methods will not make the host application stop and will not print out logs to console.
If **debuggable** parameter is **true** then output to logging will be provided.

 * Gather some basic information about device: android version and name and language settings.
  Method is being called once, when sdk is being triggered onCreate. If you wish, you can call it from anywhere in application

```kotlin
    Telemetrics.deviceData()
```

 * Store information about encounter that was used to invoke interaction.
 As this is not automatically possible, manual input is necessary from user
 Please refer to documentation[2] about data types
 
```kotlin
    Telemetrics.encounter("trigger_tag", "completionStatus", "triggerTag","levelName", "difficultyLevelName", "difficultyEstimation")
```

[1]: https://github.com/themonetizr/monetizr-android-sdk
[2]: https://docs.themonetizr.com/android/