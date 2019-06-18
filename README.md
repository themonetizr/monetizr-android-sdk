# Monetizr Android SDK library

## What is Monetizr?

 Monetizr is a game reward engine, we drive revenue to your business and enhance the experience of your players!
   
 Monetizr rewards your users with an opportunity to unlock and buy your own game merchandise (t-shirts, hats, 3d-figurines, decals, and 40+ other products),
 gift cards (Amazon, Apple, etc.), and even brand sponsored rewards (from brands that fit your core audience and goes well 
 with the narrative of the game). How you want to monetize, you decide!
 
## Monetizr Android SDK

Monetizr Android SDK is a library project built upon Monetizr RESTful API to allow smoother integration with Monetizr

Monetizr SDK is written using singleton pattern (or anti-pattern). The choice was obvious, it allows easier integration and does not require hardcore set-up.


### Setting up

Monetizr SDK is published using jCenter. You can, of course download/clone repozitory and add it as a module dependecy to your project.

At this point I am going through publishing process with jCenter and will provide complete instructions for integration using it

Monetizr is providing a per-client based API key`s. We are using oAuth 2 authentication and simplifying that for our client SDKs.
API is a public two-way, that does not expose any useful information, but still be aware of this limitation.
API settings are being provided in-application.

Library can be added to your project in couple of ways.
 
1. Method 1

 * Open your project in Android Studio

 * Download the library (using Git, or a zip archive to unzip)
   
 * Go to File > Import Module and import the library as a module

 * Right-click your app in project view and select "Open Module Settings"

 * Click the "Dependencies" tab and then the '+' button

 * Select "Module Dependency"

 * Select "Library Project"

 * Modify the library if you want to


2. Method 2

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


3. Method 3, Preferred for most cases

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


### Using library inside application

Application does need its itial settings: API key provided by Monetizr team


```kotlin
MonetizrSdk.showProductForTag("monetizr-sample-t-shirt")
```