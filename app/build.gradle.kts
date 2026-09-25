import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)

  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

fun resolveSecret(key: String): String {
  val isRealValue: (String?) -> Boolean = { v ->
    !v.isNullOrEmpty() &&
      !v.startsWith("YOUR_") &&
      !v.startsWith("PASTE_") &&
      !v.startsWith("$") &&
      !v.startsWith("<") &&
      !v.equals("null", ignoreCase = true) &&
      !v.equals("undefined", ignoreCase = true) &&
      !v.equals("MY_API_KEY", ignoreCase = true)
  }

  // 1. Check root .env file (local secrets, gitignored)
  val envFile = rootProject.file(".env")
  if (envFile.exists()) {
    val props = Properties()
    FileInputStream(envFile).use { props.load(it) }
    val v = props.getProperty(key)?.trim()
    if (isRealValue(v)) return v!!
  }

  // 2. Check System Environment variables (GitHub Actions secrets / AI Studio environment)
  val envVal = System.getenv(key)?.trim()
  if (isRealValue(envVal)) return envVal!!

  // 3. Check root .env.example file (fallback template)
  val exampleFile = rootProject.file(".env.example")
  if (exampleFile.exists()) {
    val props = Properties()
    FileInputStream(exampleFile).use { props.load(it) }
    val v = props.getProperty(key)?.trim()
    if (isRealValue(v)) return v!!
  }

  return ""
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.motorwinding.qrxpz"
    minSdk = 24
    targetSdk = 36
    val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
    val calcVersionCode = if (githubRunNumber != null) githubRunNumber + 3 else 3
    val calcVersionName = "${calcVersionCode}.0"

    versionCode = calcVersionCode
    versionName = calcVersionName

    val imgbbApiKey = resolveSecret("IMGBB_API_KEY")
    buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbApiKey\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    getByName("debug") {
      val keystorePath = System.getenv("DEBUG_KEYSTORE_PATH") ?: "${rootDir}/debug.keystore"
      storeFile = file(keystorePath)
      storePassword = System.getenv("DEBUG_STORE_PASSWORD") ?: "android"
      keyAlias = System.getenv("DEBUG_KEY_ALIAS") ?: "androiddebugkey"
      keyPassword = System.getenv("DEBUG_KEY_PASSWORD") ?: "android"
    }
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
  ignoreList.add("IMGBB_API_KEY")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  // implementation(libs.firebase.storage)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  // implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  // "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}
