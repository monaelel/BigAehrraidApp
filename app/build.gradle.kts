import java.util.Properties

val localProps = Properties().also { props ->
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(props::load)
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bigaehrraidapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.bigaehrraidapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = localProps.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "MAPS_API_KEY", "\"${localProps.getProperty("MAPS_API_KEY", "")}\"")
        // Stripe – publishable key is safe on-device; secret key is here for TEST only.
        // For production, remove STRIPE_SECRET_KEY and call your own backend instead.
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"${localProps.getProperty("STRIPE_PUBLISHABLE_KEY", "")}\"")
        buildConfigField("String", "STRIPE_SECRET_KEY", "\"${localProps.getProperty("STRIPE_SECRET_KEY", "")}\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.google.firebase.auth)
    implementation(libs.glide)
    implementation(libs.firebase.database)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation(libs.stripe.android)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
