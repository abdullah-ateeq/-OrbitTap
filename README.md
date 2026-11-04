# OrbitTap 🪐

**OrbitTap** is a fast-paced, arcade timing game for Android built entirely with **Jetpack Compose**. Test your reflexes and precision by tapping the screen when the orbiting object aligns with the dynamic target zone!

---

## 🌟 Key Features

- 🎮 **Intuitive One-Tap Gameplay**: Simple to learn, challenging to master. Tap anywhere on the screen at the exact moment the orbiter enters the target arc.
- ⚡ **Dynamic Progressive Difficulty**: As your score and hit streak increase, speed ramps up, target arcs narrow down, and rotation directions switch dynamically.
- 🎯 **Precision Scoring & Combos**:
  - **Standard Hits** vs **Perfect Hits** depending on alignment accuracy.
  - Streak multipliers (2x, 3x, etc.) for continuous successful taps.
  - Floating score popups and dynamic visual feedback.
- 🎨 **Custom Jetpack Compose Canvas**:
  - Smooth 60–120 FPS game loop powered by Kotlin Coroutines & `StateFlow`.
  - Dynamic particle explosions on hits and perfect hits.
  - Screen-shake animations and hit pulse effects on misses.
- 🔊 **Audio & Haptic Feedback**:
  - Low-latency synthesized sound engine using Android's native `AudioTrack`.
  - Tactile haptic feedback for hits, perfect hits, and misses.
- 🌓 **Themes & Settings**:
  - Custom color palettes with Dark, Light, and System Default themes.
  - Toggle audio and haptics preferences stored via Jetpack DataStore.
  - Reset stats and view lifetime high scores and best streaks.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture**: Modern MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **State Management**: Kotlin `StateFlow`, `SharedFlow`, and Coroutines
- **Navigation**: Jetpack Compose Navigation
- **Local Storage**: Jetpack DataStore Preferences
- **Audio & Haptics**: Native `AudioTrack` synthesizer & Android `Vibrator` API
- **Testing**: JUnit4 for game physics, score logic, and progressive difficulty rules

---

## 📂 Project Structure

```
com.example.orbittap
├── audio/                   # Low-latency Audio synthesis & Haptics engines
│   ├── GameAudioEngine.kt
│   └── GameHaptics.kt
├── data/                    # User preferences DataStore repository & model
│   ├── UserPreferences.kt
│   └── UserPreferencesRepository.kt
├── game/                    # Core game loop logic, math calculations, and ViewModel
│   ├── DifficultyManager.kt
│   ├── GameMath.kt
│   ├── GameState.kt
│   └── GameViewModel.kt
└── ui/                      # Jetpack Compose UI
    ├── components/          # Custom Canvas playfield & preview rendering
    │   ├── OrbitPlayfieldCanvas.kt
    │   └── OrbitPreviewCanvas.kt
    ├── navigation/          # Jetpack Compose NavHost graph
    │   ├── OrbitTapNavGraph.kt
    │   └── Screen.kt
    ├── screens/             # Home, Game, Result, Settings, and About screens
    │   ├── HomeScreen.kt
    │   ├── GameScreen.kt
    │   ├── ResultScreen.kt
    │   ├── SettingsScreen.kt
    │   └── AboutScreen.kt
    └── theme/               # OrbitTap Material 3 dark/light themes & color system
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java 11 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 37 (Android 15)

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/-OrbitTap.git
   cd -OrbitTap
   ```

2. **Open in Android Studio**:
   Open Android Studio and choose **Open an Existing Project**, selecting the cloned folder.

3. **Build & Run**:
   Select an Android Emulator or connected physical device and click **Run** (`Shift + F10`).

---

## 🧪 Running Tests

To run the unit tests covering game rules, math calculations, and difficulty scaling:

```bash
./gradlew test
```

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
