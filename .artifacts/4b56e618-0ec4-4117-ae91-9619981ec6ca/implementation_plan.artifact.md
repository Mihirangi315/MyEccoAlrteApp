# Implementation Plan - EchoAlert UI Layer

Build the UI layer for EchoAlert, including the main dashboard, settings screen, and basic navigation.

## User Review Required

> [!IMPORTANT]
> I will be placing the Detection History (RecyclerView) directly on the Main screen as requested in the layout instructions for `activity_main.xml`, effectively combining the "Home" and "History" views.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_main.xml](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/res/layout/activity_main.xml)
- Add App Title.
- Add "Start Listening" / "Stop Listening" MaterialButton.
- Add RecyclerView for detection history.
- Add a Settings icon/button in the top corner for navigation.

#### [NEW] [activity_settings.xml](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/res/layout/activity_settings.xml)
- Add 5 MaterialSwitches for sound targets (Doorbell, Alarm, Baby Crying, Phone Ringing, Knock).
- Add a SeekBar for sensitivity.
- Add a "Back" button to return to Main.

#### [NEW] [item_detection.xml](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/res/layout/item_detection.xml)
- Define how a single detection event looks in the list (Sound name and Timestamp).

### Logic & Navigation

#### [MODIFY] [MainActivity.kt](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/java/com/example/myeccoalrteapp/MainActivity.kt)
- Wire up the toggle button to start/stop `ListeningService`.
- Initialize the RecyclerView with a dummy list (to be replaced by Room later).
- Implement navigation to SettingsActivity.

#### [NEW] [SettingsActivity.kt](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/java/com/example/myeccoalrteapp/SettingsActivity.kt)
- Handle the switch states and SeekBar (just UI for now).
- Handle the back button navigation.

#### [NEW] [DetectionEvent.kt](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/java/com/example/myeccoalrteapp/DetectionEvent.kt)
- Simple data class for history items.

#### [NEW] [DetectionAdapter.kt](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/java/com/example/myeccoalrteapp/DetectionAdapter.kt)
- RecyclerView Adapter to display detection events.

### Configuration

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/User/AndroidStudioProjects/MyEccoAlrteApp/app/src/main/AndroidManifest.xml)
- Register `SettingsActivity`.
- Declare `ListeningService` (even if not implemented yet, so the Intent works).
- Add necessary permissions (POST_NOTIFICATIONS, RECORD_AUDIO, VIBRATE, FOREGROUND_SERVICE).

## Verification Plan

### Automated Tests
- N/A (Focus on UI implementation)

### Manual Verification
- Deploy to emulator/device.
- Verify "Start/Stop" button changes state and triggers service Intent (check logcat).
- Verify navigation to Settings screen and back.
- Verify RecyclerView shows dummy detection data.
- Check layout rendering for switches and slider.
