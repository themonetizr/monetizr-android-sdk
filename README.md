## What is Monetizr?
Monetizr is a turn-key platform for game developers enabling to sell or give-away game gear right inside the game's UI. You can use this SDK in your game to let players purchase products or claim gifts within the game.  All orders made with Monetizr automatically initiates fulfillment and shipping. More info: https://docs.themonetizr.com/docs/get-started.
 
## Monetizr Android SDK
Monetizr Android SDK is a plugin with the built-in functionality of:
- showing image carousel and fullscreen pictures in offers to end-users;
- HTML texts for descriptions;
- allowing end-users to select product variant options;
- displaying price in real or in-game currency (including discounts);
- checkout and payment support.

Monetizr uses Stripe adn Google Pay integrations for payments processing. To use SDK and connect to Monetizr servers, all you need is a single API key.
It can be retrieved via Monetizr web [Console][3].
The API is an unauthenticated public API - no username or password is required. 
Only an access token is required. You can use Monetizr's public test token or create your own [Console][3]. The API does not expose any data that can be potentially harmful or can be considered as personal data.

Read the Monetizr's [Android documentation][2] to find out more.

## Installation
### Option 1 (suggested)
**Gradle:**

```gradle
repositories {
  mavenCentral()
  google()
}

dependencies {
  implementation 'io.monetizr.monetizrsdk:MonetizrSDK:2.1.0'
}
```

**Or Maven:**

```xml
<dependency>
    <groupId>io.monetizr.monetizrsdk</groupId>
    <artifactId>MonetizrSDK</artifactId>
    <version>2.1.0</version>
    <type>pom</type>
</dependency>
```
 
### Option 2
 * Open your project in Android Studio;
 * Download the library using Git, or a zip archive to unzip;
 * Go to File > Import Module and import the library as a module;
 * Right-click your app in project view and select "Open Module Settings";
 * Click the "Dependencies" tab and then the '+' button;
 * Select "Module Dependency";
 * Select "Library Project";
 * Modify the library if necessary.

### Option 3
 * Open your project in Android Studio;
 * Download the library using Git, or a zip archive to unzip;
 * Create a folder "subProject" in your project;
 * Copy and paste the library folder to your subProject folder;
 * On the root of your project directory, create/modify the settings.gradle file. It should contain something like the following:
 
```gradle
include 'MyApp', ':monetizrsdk'

``` 
 * gradle clean & build/close the project and reopen/re-import it;
 * Edit your project's build.gradle to add the following in the `dependencies` section:
```gradle
dependencies {
//...
    implementation project(':monetizrsdk')
}
```

## Using the library in your app
To use the SDK you need an [API key][4]. For testing purposes, you can use public test key `4D2E54389EB489966658DDD83E2D1`.
API access key must be included in manifest.xml file.

```xml
<meta-data android:name="monetizr_api_key" android:value="4D2E54389EB489966658DDD83E2D1"/>
```

To show a product in an [Offer View][5], you need to call product_tag. Product tags represent a specific product, and they are managed in the web Monetizr Console. For testing purposes, you can use public test product `T-shirt`.

When you are ready to show an Offer View inside your app, you can invoke Monetizr SDK static method from within your app. One call invokes one Offer View.

```kotlin
MonetizrSdk.showProductForTag("T-shirt")
```

To display product as "locked" you need pass additional parameter after product tag. Locked parameter can be `true` or `false` (default). Doing so will alter product image view with locked symbol on top of product and checkout/claim/purchase button will be disabled.

```kotlin
MonetizrSdk.showProductForTag("T-shirt", true)
```

Monetizr supports giveaway campaigns for clients who pre-pay products instead of players. Giveaway campaigns currenctly require manual approval and a signed agreement. To learn more, contact <martins@themonetizr.com>.

When using giveaway campaigns you will need to pass additional information to confirm players ability to claim this product. In this approach, two additional parameters are required - status of "locked" and a player ID. The later will be used to confirm with your servers whether the player who initiates Offer View can retrieve product for free.

```kotlin
MonetizrSdk.showProductForTag("T-shirt", false, "Player ID")
```

The library defines two permissions: access to the internet and necessity to check for internet connection.

```xml
 <uses-permission android:name="android.permission.INTERNET"/>
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```
## Optional settings
### API debugging 

Set to `false` by default. SDK methods are calling RESTful API and safely storing provided bits of the data using queued HTTPS requests. If something fails, the methods do not stop the host application and do not print out logs to console. If `debuggable` is set to `true`, then output is provided to Logging information.

```kotlin
MonetizrSdk.debuggable = true
```

[1]: https://github.com/themonetizr/monetizr-android-sdk
[2]: https://docs.themonetizr.com/docs/android
[3]: https://app.themonetizr.com/
[4]: https://docs.themonetizr.com/docs/creating-account#section-your-unique-access-token
[5]: https://docs.themonetizr.com/docs/offer-view
