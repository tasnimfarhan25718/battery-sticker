# Battery Sticker App 🔋🐾

An Android application that displays cute decorative pet stickers (like Oggy, Frog, Bear) directly above your phone's battery percentage and status bar notch.

## ✨ Features
1. **Dynamic Battery Overlay:** Floats seamlessly over the status bar and notch using `SYSTEM_ALERT_WINDOW`.
2. **Charging Indicator:** Automatically detects when the phone is plugged in and displays a lightning/sparkle badge on the sticker!
3. **Custom Alignment Sliders:**
   - **Horizontal (X) Offset:** Move left/right to perfectly align with your phone's battery icon.
   - **Vertical (Y) Offset:** Move up/down to sit right on top of the status bar.
   - **Size Scale:** Adjust from small (24dp) to large (90dp).
4. **Preset Characters:**
   - 🐱 **Oggy Cat:** Classic cartoon cat peeking over the battery.
   - 🐸 **Cute Frog:** Green blushing frog.
   - 🐻 **Teddy Bear:** Cozy brown bear.
5. **Auto-Restart on Boot:** Automatically reactivates after restarting your phone.

---

## 🚀 How to Build & Install on your Phone

### Method 1: Open in Android Studio (Recommended)
1. Download & Install [Android Studio](https://developer.android.com/studio) if you don't already have it.
2. Open Android Studio -> Click **Open** -> Select this project folder (`BatteryStickerApp`).
3. Connect your Android phone via USB (with **USB Debugging** enabled in Developer Options) or use an Emulator.
4. Click the green **Run (▶)** button.
5. Grant the **"Appear on top" / "Display over other apps"** permission inside the app.
6. Toggle **Enable Battery Sticker** and adjust the sliders!

### Method 2: Build APK via Gradle
Open a terminal inside this directory and run:
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
You can transfer this APK to your phone and install it directly!
