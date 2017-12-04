Jyunioni App
![alt text](http://users.jyu.fi/~jatasuor/Jyunioni/Jyunioni-banneri "Jyunioni banneri")
===================================

This app is for the use of students in the University of Jyväskylä.

**Download the app in Google Play:**

The app has two tabs. First tab has an event calendar which compiles 4 organisations publicly available events from their website calendars. Events are shown in a list in ascending order by the time they begin. Clicking an event opens up event details page where is additional information of the event. From the event details the user can also go to the official events webpage for further information.

These organizations are the following:
* Linkki Jyväskylä Ry.
* Pörssi Ry.
* Dumppi Ry.
* Stimulus Ry.

If you wish your organizations calendar to be featured in the app, please contact me via [email](https://github.com/januzgi/Jyunioni-app/tree/master/app/src/main#support--contact "See Support & Contact").

The second tab is a real-time shoutbox that requires a Google log-in. Messages being sent is spread across all users of the app. Google log-in is by default required to use the app. 

The app is by default in Finnish and also localized for English users. The data of the events is shown in Finnish.


Known issues
-------
* If you start the app without internet connection it asks you to connect to either: connect to the internet via WIFI, 4G or quit. After connected to a WIFI or 4G the app needs to be restarted to fetch the events.


Support & Contact 
-------

For issues, requests or feedback, please contact me via email:
janisuoranta@icloud.com


- - - -


Developers guide
------

Are you interested in developing the app further? Want to build something on top of it? You may contact me via [email](https://github.com/januzgi/Jyunioni-app/tree/master/app/src/main#support--contact "See Support & Contact") if you have some suggestions for improvements in the app.

To develop the app I recommend using [Android Studio](https://developer.android.com/studio/index.html "Download Android Studio"). 

Easiest way to get started with Jyunioni would be to copy the classes into your own project. 


1. At first, you need to create a new project in Android Studio.
Name the project as something else than "jyunioni" to use Firebase console later. 
Choose your project to target API 15 and later (IceCreamSandwich) on Phone / Tablet. 
Choose to "Add No Activity" to the project in the creation process.
    * Guide for creating a new project in Android Studio can be found [here](https://developer.android.com/studio/projects/create-project.html "Creating a new project in Android Studio").


2. To get the classes pull this repository to your local machine: `git pull https://github.com/januzgi/Jyunioni-app`
    * Guide for creating a local repository can be found [here](https://www.atlassian.com/git/tutorials/setting-up-a-repository "How to set up a repository using git").


3. There are two Gradle files that need to be modified for running Jyunioni.
    1) build.gradle (Project: MyApplication)
    2) build.gradle (Module: app)

The following codes are the contents of the two Gradle files. After making the changes remember to synchronize the project with "Sync Now" that appears in the top right when editing the .gradle files. *The sync won't be successful until you have the "**google-services.json**" at place so see **step 5)** for that.*

1) **build.gradle (Project: MyApplication)**
```java 
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        // You need to add the following repository to download the
        // new plugin.
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath 'com.google.gms:google-services:3.1.0'
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

2) **build.gradle (Module: app)**
```java
apply plugin: 'com.android.application'

android {
    compileSdkVersion 26
    buildToolsVersion '26.0.2'

    defaultConfig {
        applicationId "jani.suoranta.jyunioni"
        minSdkVersion 15
        targetSdkVersion 23
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:26.1.0'
    compile 'com.android.support:support-v4:26.1.0'
    compile 'com.android.support:design:26.1.0'
    compile 'com.github.bumptech.glide:glide:3.7.0'
    compile 'de.hdodenhof:circleimageview:1.3.0'

    // Google
    compile 'com.google.android.gms:play-services-auth:11.4.2'

    // Firebase
    compile 'com.google.firebase:firebase-analytics:11.4.2'
    compile 'com.google.firebase:firebase-database:11.4.2'
    compile 'com.google.firebase:firebase-storage:11.4.2'
    compile 'com.google.firebase:firebase-auth:11.4.2'
    compile 'com.google.firebase:firebase-config:11.4.2'
    compile 'com.google.android.gms:play-services-appinvite:11.4.2'
    compile 'com.google.firebase:firebase-messaging:11.4.2'
    compile 'com.google.android.gms:play-services-ads:11.4.2'
    compile 'com.google.firebase:firebase-crash:11.4.2'
    compile 'com.google.firebase:firebase-appindexing:11.4.2'

    // Firebase UI
    compile 'com.firebaseui:firebase-ui-database:3.0.0'
}

apply plugin: 'com.google.gms.google-services'
repositories {
    google()
}
buildscript {
    repositories {
        google()
    }
}
```


4. Once you have set up your new project in Android Studio, have synced the gradle succesfully and have the classes locally in your machine, you can copypaste the Jyunioni classes, folders and the manifest.

**Put:**

* all folders and their content from Jyunioni-app/app/src/main/res/ into `/MyApplication/app/src/main/res`. This means you can delete all the default folders in `res/` before copypasting the Jyunioni repository `res/` folders.

* .java classes into `/MyApplication/app/src/main/java/com/example/android/myapplication/`

* AndroidManifest.xml into `/MyApplication/app/src/main/`

You need to update the package name from `jani.suoranta.jyunioni` to `com.example.android.MyApplication` in various lines in **AndroidManifest.xml**.

You also need to update every .java file package declaration to `package com.example.android.myapplication;`. These declarations are also at some parts of the code, so just follow the red error indicators.


5. The app uses Google's Firebase and its database for the real-time chat. With this tutorial you will get the "google-services.json" file needed to sync the gradle succesfully. To get using Firebase in the app yourself, see this [guide](https://codelabs.developers.google.com/codelabs/firebase-android/#0 "Firebase real-time chat in Android guide").

Happy coding!🎉🔥


**The server side code which is .PHP and handles the parsing of the events data can be found in it's [own repository](https://github.com/januzgi/Jyunioni-app-server "Jyunioni server PHP codes").**


If you need further assistance or have any questions, feel free to contact me via [email](https://github.com/januzgi/Jyunioni-app/tree/master/app/src/main#support--contact "See Support & Contact").
