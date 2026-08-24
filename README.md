# EchoAlert 🔊🔔

**EchoAlert** is a real-time, sound-based accessibility application designed for users who are deaf or hard-of-hearing. The app acts as an "extra set of ears," monitoring the environment for critical everyday sounds and providing immediate haptic and visual feedback.

---

## 🌟 Key Features

- **Real-time Monitoring**: Uses a Foreground Service to listen for sounds even when the app is in the background or the screen is locked.
- **ML-Powered Detection**: Leverages the **YAMNet** audio classification model (TensorFlow Lite) to identify specific sounds:
    - 🔔 Doorbell
    - ⏰ Alarms/Sirens
    - 👶 Baby Crying
    - 📞 Phone Ringing
    - ✊ Door Knocks
- **Accessibility Alerts**: Immediate alerts via high-priority notifications and distinct vibration patterns.
- **Detection History**: A local log of all detected sounds with timestamps and icons for easy review.
- **Customizable Settings**: Toggle specific sound targets on/off and adjust microphone sensitivity.
- **Modern UI**: Built with **Material 3**, featuring a pulsing "Listening" status indicator and intuitive iconography.

---

## 🏗️ Architecture

The project is structured into 5 distinct layers to facilitate team collaboration:

1.  **UI Layer**: Manages the Dashboard, Settings, and History screens (using RecyclerView and Material Components).
2.  **Audio Capture Layer**: Handles raw audio input (16kHz Mono 16-bit PCM) using the `AudioRecord` API.
3.  **ML Classification Layer**: Processes audio buffers through a pre-trained YAMNet TFLite model.
4.  **Data Layer**: Manages local persistence for settings and detection history using **Room**.
5.  **Alert Layer**: Orchestrates user-facing alerts (NotificationManager and Vibrator API).

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Asynchrony**: Kotlin Coroutines
- **UI Framework**: Jetpack Compose / Material Design 3
- **Machine Learning**: TensorFlow Lite (YAMNet)
- **Database**: Room Persistence Library
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 37

---

## 🚀 Getting Started

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/EchoAlert.git
    ```
2.  **Open in Android Studio**:
    - Ensure you have **Android Studio Ladybug** (or newer) installed.
    - Sync the project with Gradle files.
3.  **Permissions**:
    - The app requires `RECORD_AUDIO` and `POST_NOTIFICATIONS` (on Android 13+) permissions to function.
4.  **Run**:
    - Deploy to a physical device or emulator with microphone support.

---

## 🎓 About
This project was developed as part of a university group project over a 1-week timeline by a team of 5 members.
