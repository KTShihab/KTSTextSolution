# KTS Text Solution

[![Latest Release](https://img.shields.io/github/v/release/KTShihab/KTSTextSolution?color=blue&label=Latest%20Version)](https://github.com/KTShihab/KTSTextSolution/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-green)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin%20%7C%20Jetpack%20Compose-purple)](https://developer.android.com/jetpack/compose)

**KTS Text Solution** is an advanced, accessible text reading, editing, and formatting workstation for Android. Designed from the ground up with first-class **TalkBack screen reader accessibility**, it empowers users to navigate, edit, style, and process long documents, articles, and clipboard contents with precision.

---

## 📥 Download

Get the latest official APK directly:

👉 **[Download KTSTextSolution.apk (Latest Version)](https://github.com/KTShihab/KTSTextSolution/releases/latest/download/KTSTextSolution.apk)**

You can also browse all version updates and changelogs on the [Releases Page](https://github.com/KTShihab/KTSTextSolution/releases).

---

## ✨ Key Features

### 📖 Multi-Granularity Reading & Navigation
- **Granular Splitting**: Break down text by **Paragraph**, **Sentence**, **Line**, **Word**, or **Character**.
- **Playback Controls**: Navigate chunks with **Previous**, **Play / Pause**, and **Next** buttons that synchronize with your active navigation mode.
- **Built-in Text-To-Speech (TTS)**: Clean, customizable speech output with adjustable speech speed, pitch, and voice settings.

### 🌐 Instant Web Article Extractor
- Copy any webpage URL and open KTS Text Solution.
- Automatically extracts the main article text while eliminating ads, navigations, sidebars, and clutter for an unobstructed reading experience.

### 🎧 Background Auto-Reader
- Automatically reads aloud copied text from apps like WhatsApp and Telegram while running seamlessly in the background.
- Includes a dedicated background reader service with persistent playback notifications.

### ✏️ Advanced Editing & Sub-Chunk Window
- Tap any chunk to edit it in place.
- **Open in New Window**: Split any chunk further into smaller units (e.g., a paragraph into sentences or words) and edit with fine-grained control.
- Full text manipulation: Cut, Copy, Paste, Delete, and multi-level Undo.

### 🎯 Smart Selection Mode
- Range selection support: **Select from Start to Here**, **Select from Here to End**, and arbitrary range selection between two chunks.
- Batch operations: Copy, Cut, Delete, or Format all selected chunks at once.

### 🎨 Rich Text Formatting
- **Font Size**: Adjustable font size slider and +/- controls, with an option to restore app defaults.
- **Styles**: Toggle **Bold**, *Italic*, and <u>Underline</u>.
- **Text Color**: High-contrast theme colors including Red, Blue, Green, Orange, Purple, Teal, Yellow, Dark, and White.
- **Alignment**: Align text to **Left**, **Center**, **Right**, or **Justify**.
- **Case Change**:
  - **UPPERCASE**: Convert text to all capital letters.
  - **lowercase**: Convert text to all small letters.
  - **Title Case**: Capitalize the first letter of each word.
  - **Journal / Sentence Case**: Capitalize the initial letter of each sentence.
- **Bullets & Numbering**:
  - **• Bullet List**: Apply bullet markers.
  - **1. Numbered List**: Sequentially number selected items (`1. `, `2. `, `3. `...).
  - **Remove List**: Cleanly strip existing bullets and numbers.
- **Live Preview**: Real-time visual preview card reflecting all formatting changes dynamically.

### ℹ️ Text Format Information Inspector
- Focus on any text chunk and open **"Text Format Information"** from the Action Menu or via TalkBack Custom Accessibility Actions.
- Inspect font size, applied colors, alignment, styles, split mode, document position, and detailed text metrics (character count, word count, and line count).
- Includes automatic spoken summary for TalkBack users.

### 📑 Multi-Document Sessions
- Work with multiple text documents simultaneously using multi-session tabs.
- Switch, create, and close sessions without losing work or clipboard history.

### 📋 Clipboard History & Favorites
- Searchable clipboard history logs automatically.
- Save frequently used snippets to **Favorites** organized into custom categories and folders.
- Full backup and restore capabilities (JSON export/import).

### 🔍 Find and Replace
- Search text with live match counting.
- Single match Replace and **Replace All** with Undo history.

### ♿ Accessibility-First Design (TalkBack)
- 100% accessible with Android TalkBack.
- Native **Custom Accessibility Actions** for swipe-up/down gesture menus.
- Clear accessibility labels, state announcements, and tactile haptic feedback.

### 🔄 In-App GitHub Updates
- Check for updates directly within the app (**More Options → Check for Updates**).
- One-click download and installation directly from GitHub Releases.

---

## 🛠️ Tech Stack & Requirements

- **Operating System**: Android 8.0 (Oreo, API level 26) and above
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) with Android Architecture Components
- **Persistence**: Android DataStore Preferences & JSON Storage
- **Build System**: Gradle 8.11+ / AGP 8.8+

---

## 🚀 Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/KTShihab/KTSTextSolution.git
   ```
2. Open the project in **Android Studio** (Koala, Ladybug, or newer).
3. Allow Gradle to synchronize dependencies.
4. Assemble the debug APK:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
5. Deploy to your connected Android device or emulator.

---

## 👨‍💻 Developer & Support

- **Developer**: Shihab KT
- **Email**: [shihabkt08@gmail.com](mailto:shihabkt08@gmail.com)
- **Repository**: [https://github.com/KTShihab/KTSTextSolution](https://github.com/KTShihab/KTSTextSolution)

---

## 📄 License

Copyright © 2026 Shihab KT. All rights reserved.
