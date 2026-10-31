# AURA
 
An Android app that reads your emotional state from your voice and your smartwatch, then helps you calm down with music, breathing exercises, and an AI companion. Built with Kotlin and Jetpack Compose, and works offline.
 
## Features
 
- **Voice emotion analysis** in Vietnamese, with five modes: standard, stress, anxiety, ADHD and sleep. Uses Gemini when online, and on-device Vosk plus keyword scoring when offline.
- **Biometrics** (heart rate, SpO2, sleep, steps) from Gadgetbridge or Blynk
- **AI chat** that sees your vitals and plays a favourite track when you sound stressed
- **Healing music**, a **breathing guide**, **stress trend charts** and **daily AI reports**
- **Trend alerts**, plus an optional Google Sheets webhook and an emergency SOS call
## Setup
 
1. Open the project in Android Studio.
2. Copy `.env.example` to `.env` and set `GEMINI_API_KEY`. `BLYNK_TOKEN` and `WEBHOOK_URL` are optional.
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`, or add your own `debug.keystore`.
4. Run on a device with Android 7.0 or newer.
Keys can also be changed later in the app's Settings screen.
 
> AURA is a wellbeing tool, not a medical device.
 
