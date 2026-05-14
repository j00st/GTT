#!/bin/bash
set -e

mkdir -p app/src/main/java/com/example/gtt/data
mkdir -p app/src/main/java/com/example/gtt/service
mkdir -p app/src/main/java/com/example/gtt/ui
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/mipmap-anydpi-v26
mkdir -p gradle/wrapper

cat << 'EOF' > README.md
# Geofence Time Tracker (GTT)

GTT is a "Universal Location-Based Time Logger." It automatically tracks hours spent at specific zones (Office, Library, Client site) using geofencing.

1. **Define:** User drops a pin and sets a radius (e.g., 100m).
2. **Detect:** App uses OS-level geofencing (low battery) to log entry/exit.
3. **Dashboard:** User sees aggregated "Hours today/this week/this month" per location.
4. **Active Tracking:** A persistent notification appears when "on-site" with a manual "Punch Out" button.
EOF

cat << 'EOF' > .gitignore
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
EOF

cat << 'EOF' > .cursorrules
# GTT Architecture Rules
* **NO GPS POLLING:** Use `GeofencingClient` transition intents only.
* **ROOM 3.0:** Use `androidx.room` (simulating room3) namespace with `suspend`/`Flow` DAOs.
* **DEBOUNCE:** Implement a 5-minute exit debounce to prevent GPS flapping.
EOF

cat << 'EOF' > settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "GTT"
include(":app")
EOF

cat << 'EOF' > build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKsp) apply false
}
EOF

cat << 'EOF' > gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF

cat << 'EOF' > gradle/libs.versions.toml
[versions]
agp = "8.3.2"
kotlin = "1.9.23"
ksp = "1.9.23-1.0.19"
coreKtx = "1.13.1"
junit = "4.13.2"
androidxTestExtJunit = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.8.3"
activityCompose = "1.9.0"
composeBom = "2024.06.00"
room = "2.6.1"
playServicesLocation = "21.3.0"
workManager = "2.9.0"
navigationCompose = "2.7.7"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExtJunit" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlinKsp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
EOF

cat << 'EOF' > app/build.gradle.kts
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKsp)
}

android {
    namespace = "com.example.gtt"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gtt"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    implementation(libs.play.services.location)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
EOF

cat << 'EOF' > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

    <application
        android:name=".GttApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.GTT"
        tools:targetApi="35">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.GTT">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <receiver
            android:name=".service.GeofenceBroadcastReceiver"
            android:enabled="true"
            android:exported="true"/>
            
        <service
            android:name=".service.TrackingForegroundService"
            android:foregroundServiceType="location"
            android:exported="false"/>
            
    </application>

</manifest>
EOF

mkdir -p app/src/main/res/xml
cat << 'EOF' > app/src/main/res/xml/backup_rules.xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="."/>
    <include domain="database" path="."/>
</full-backup-content>
EOF

cat << 'EOF' > app/src/main/res/xml/data_extraction_rules.xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="."/>
        <include domain="database" path="."/>
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="."/>
        <include domain="database" path="."/>
    </device-transfer>
</data-extraction-rules>
EOF

cat << 'EOF' > app/src/main/res/values/strings.xml
<resources>
    <string name="app_name">GTT</string>
</resources>
EOF

cat << 'EOF' > app/src/main/res/values/themes.xml
<resources>
    <style name="Theme.GTT" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
EOF

# Fetch gradle wrapper
curl -sL https://services.gradle.org/distributions/gradle-8.7-bin.zip -o gradle.zip
unzip -q gradle.zip
./gradle-8.7/bin/gradle wrapper --gradle-version 8.7
rm -rf gradle.zip gradle-8.7
EOF
