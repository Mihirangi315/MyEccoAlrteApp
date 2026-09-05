# EchoAlert 🔊🔔

**EchoAlert** is a real-time, sound-based accessibility application designed for users who are deaf or hard-of-hearing. The app acts as an "extra set of ears," monitoring the environment for critical everyday sounds and providing immediate haptic and visual feedback.

---

## 🌟 Key Features

- **Real-time Monitoring**: Uses a Foreground Service to listen for sounds even when the app is in the background or the screen is locked.
- **Nitro-Speed Detection**: Implements a **Sliding Window** algorithm to analyze audio every **100ms**, ensuring near-instant response times.
- **ML-Powered Classification**: Leverages the **YAMNet** audio classification model (TensorFlow Lite) to identify:
    - 🔔 Doorbell
    - ⏰ Alarms/Sirens/Smoke Detectors
    - 👶 Baby Crying
    - 📞 Phone Ringing/Ringtones
    - ✊ Door Knocks
- **Smart Alerts**: 
    - **Continuous Mode**: Optional repeating haptic alerts for critical sounds.
    - **One-Tap Dismiss**: Quickly silence alerts via a notification action button.
    - **Audible Ringtone**: User-configurable sound alerts.
- **Detection History**: A persistent log of all sound events with icons and timestamps, powered by **Room**.
- **Modern UI**: Built with **Material 3**, featuring a pulsing "Listening" status indicator and a live sensitivity percentage display.

---

## 🏗️ Architecture

The project follows a modular 5-layer architecture:

1.  **UI Layer**: Dashboard, Settings, and History screens built with Material 3 and XML Views.
2.  **Audio Capture Layer**: Low-latency raw audio input (16kHz Mono 16-bit PCM) using the `AudioRecord` API.
3.  **ML Layer**: On-device inference using TFLite and intelligent label grouping to improve accuracy.
4.  **Data Layer**: Local SQLite persistence for history and settings using the **Room** library.
5.  **Alert Layer**: Advanced haptic feedback (Waveform Vibrations) and high-priority notification management.

---

## 🚀 Performance Optimizations

To meet the requirements of a high-performance accessibility app, we implemented several key technical features:

- **Sliding Window Analysis**: By overlapping audio chunks, we increased detection resolution from 1 second to 100 milliseconds, catching brief sounds like knocks more reliably.
- **RAM Caching**: User preferences and sensitivity thresholds are cached in memory. This eliminates database latency, allowing alerts to trigger the microsecond a sound is identified.
- **Label Grouping**: We mapped 10 specific YAMNet sound indices into 5 logical categories to reduce "near-miss" classifications and increase user-facing confidence.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Asynchrony**: Kotlin Coroutines & Flow
- **UI Framework**: Material Design 3 (XML)
- **Machine Learning**: TensorFlow Lite (YAMNet Model)
- **Database**: Room Persistence Library (with KSP)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 37

---

## 🎓 About
This project was developed as part of a university group project over a 1-week timeline by a team of 5 members.

---

## 🚀 Getting Started

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/Mihirangi315/MyEccoAlrteApp.git
    ```
2.  **Add the ML Model**:
    - Download the **YAMNet TFLite** model from Kaggle.
    - Place it in `app/src/main/assets/` and rename it to `yamnet.tflite`.
3.  **Permissions**:
    - Grant `RECORD_AUDIO` and `POST_NOTIFICATIONS` permissions on launch.
4.  **Run**:
    - Deploy to a physical device for the best haptic experience.
