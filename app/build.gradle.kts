import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val prop = Properties()
val configDir = file("src/config/app.properties").inputStream()
prop.load(configDir)


android {
    namespace = "com.translate.app"
    compileSdk = 34

    defaultConfig {
        applicationId = prop.getProperty("packageName")
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        renderscriptTargetApi = 31
        renderscriptSupportModeEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
        resValue("string", "app_name", prop.getProperty("appName"))
        resValue("string", "admobId", prop.getProperty("admobId"))
        resValue("string", "facebook_app_id", prop.getProperty("facebookId"))
        resValue("string", "facebook_client_token", prop.getProperty("facebookToken"))

        buildConfigField("String", "base_url", "\"${prop.getProperty("baseUrl")}\"")
        buildConfigField("String", "config_url", "\"${prop.getProperty("configUrl")}\"")
        buildConfigField("String", "translate_url", "\"${prop.getProperty("translateUrl")}\"")
        buildConfigField("String", "privacy_url", "\"${prop.getProperty("privacyUrl")}\"")
        buildConfigField("String", "user_url", "\"${prop.getProperty("userUrl")}\"")

        buildConfigField("String", "adjust_token", "\"${prop.getProperty("adjustToken")}\"")
        buildConfigField("String", "adjust_code", "\"${prop.getProperty("adjustCode")}\"")
        buildConfigField("String", "adjust_referrerUrl", "\"${prop.getProperty("adjustReferrerUrl")}\"")
        buildConfigField("String", "adRequestCode", "\"${prop.getProperty("adRequestCode")}\"")
        buildConfigField("String", "adTapCode", "\"${prop.getProperty("adTapCode")}\"")
        buildConfigField("String", "adFillCode", "\"${prop.getProperty("adFillCode")}\"")
        buildConfigField("String", "adUnit", "\"${prop.getProperty("adUnit")}\"")
        buildConfigField("String", "adRevenue", "\"${prop.getProperty("adRevenue")}\"")
        buildConfigField("String", "adSite", "\"${prop.getProperty("adSite")}\"")
    }

    signingConfigs {
        create("release"){
            storeFile = file("src/config/${prop["storeFile"]}")
            storePassword =  prop.getProperty("storePassword")
            keyAlias = prop.getProperty("keyAlias")
            keyPassword = prop.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions("config")
    productFlavors {
        create("config") {
            setDimension("config")
        }
    }
    android.buildTypes.getByName("release").ndk.debugSymbolLevel = "NONE"
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.gms:play-services-ads-lite:22.5.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.0")
    implementation("com.google.mlkit:text-recognition-japanese:16.0.0")
    implementation("com.google.mlkit:text-recognition-korean:16.0.0")

    implementation("androidx.camera:camera-core:1.3.0-alpha02")
    implementation("androidx.camera:camera-camera2:1.3.0-alpha02")
    implementation("androidx.camera:camera-lifecycle:1.3.0-alpha02")
    implementation("androidx.camera:camera-extensions:1.3.0-alpha02")
    implementation("androidx.camera:camera-view:1.3.0-alpha02")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("com.airbnb.android:lottie-compose:6.0.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.24.7-alpha")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation ("com.adjust.sdk:adjust-android:4.33.0")
    implementation ("com.android.installreferrer:installreferrer:2.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation(project(mapOf("path" to ":imagepicker")))

    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation ("com.facebook.android:facebook-android-sdk:12.0.1")
}