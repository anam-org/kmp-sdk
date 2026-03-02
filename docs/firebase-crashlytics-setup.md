# Firebase Crashlytics Setup

This guide covers the manual steps required to complete the Firebase Crashlytics integration for Android and iOS.

## 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or select an existing project).
3. Follow the prompts to create the project. You can disable Google Analytics if not needed.

## 2. Register the Android App

1. In the Firebase Console, click **Add app** and select **Android**.
2. Enter the package name: `ai.anam.lab.client`.
3. Download the generated `google-services.json` file.
4. Place it at:
   ```
   apps/android/google-services.json
   ```
   This file is safe to commit — it contains no secrets per Firebase documentation.

## 3. Register the iOS App

1. In the Firebase Console, click **Add app** and select **iOS**.
2. Enter the bundle identifier (check Xcode target settings for the exact value).
3. Download the generated `GoogleService-Info.plist` file.
4. Place it at:
   ```
   apps/ios/App/GoogleService-Info.plist
   ```
5. In Xcode, drag the file into the `App` group and ensure **Add to targets: App** is checked.

## 4. Install iOS CocoaPods Dependencies

```bash
cd apps/ios && pod install
```

## 5. Add the dSYM Upload Build Phase (Xcode)

This step ensures that debug symbols are uploaded to Crashlytics so that crash reports show
human-readable stack traces.

1. Open `apps/ios/App.xcworkspace` in Xcode.
2. Select the **App** target.
3. Go to the **Build Phases** tab.
4. Click **+** and select **New Run Script Phase**.
5. Drag it to run **after** `[CP] Embed Pods Frameworks`.
6. Set the shell script to:
   ```bash
   "${PODS_ROOT}/FirebaseCrashlytics/run"
   ```
7. Under **Input Files**, add (per the
   [Firebase dSYM docs](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=ios)
   for Xcode 15+):
   ```
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${PRODUCT_NAME}
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Info.plist
   $(TARGET_BUILD_DIR)/$(UNLOCALIZED_RESOURCES_FOLDER_PATH)/GoogleService-Info.plist
   $(TARGET_BUILD_DIR)/$(EXECUTABLE_PATH)
   ```

## 6. Enable Crashlytics in the Firebase Console

1. In the Firebase Console, navigate to **Crashlytics** in the left sidebar.
2. Click through the onboarding flow for each app (Android and iOS).
3. The dashboard will show "Waiting for your first crash report" until a crash is received.

## 7. Verify the Integration

Crashlytics is disabled in debug builds by default (see `AnamApplication.kt` and
`AnanAppDelegate.swift`). To test:

### Android
1. Temporarily comment out the `isCrashlyticsCollectionEnabled` line in `AnamApplication.kt`.
2. Build and run: `./gradlew :apps:android:assembleDebug`.
3. Trigger a test crash (e.g., add a temporary button that calls
   `throw RuntimeException("Test crash")`).
4. Relaunch the app (crash reports are sent on next launch).
5. Check the Firebase Console — the crash should appear within ~5 minutes.
6. Restore the debug disable line after testing.

### iOS
1. Temporarily remove the `#if DEBUG` block in `AnanAppDelegate.swift`.
2. Build and run via Xcode.
3. Trigger a test crash similarly.
4. Relaunch the app and check the Firebase Console.
5. Verify dSYM upload in the Xcode build log (look for `FirebaseCrashlytics/run` output).
6. Restore the debug disable block after testing.
