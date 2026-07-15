# 📱 Mobile Lexical Analyzer (Lexer)

> A lightweight, handcrafted Lexical Analyzer implemented as a modern mobile application. 

## 📖 What is a Lexer?
A Lexical Analyzer (often called a Lexer or Scanner) is the very first phase of a compiler. It reads raw source code character by character, strips away irrelevant data like whitespace, and groups the remaining characters into meaningful, categorized sequences called **tokens** (e.g., keywords, identifiers, numbers, and symbols). 

This project was developed as a fundamental language engineering exercise. By processing source code sequentially and manually, it demonstrates the state transitions, pattern matching, and tokenization mechanics that underpin automated scanner tools (like Lex/Flex).

---

## 📸 Previews

<p align="center">
  <img src="https://github.com/user-attachments/assets/8d5c1d78-9160-45bf-b18f-2a0720389820" width="250" alt="Lexer Input Screen" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/ac14a48a-ede8-4b59-8145-99a671e95921" width="250" alt="Lexer Token Output Screen" />
</p>

---

## ✨ Language Features Handled
The custom Lexer processes a targeted, representative subset of programming syntax containing:

* **Comments:** Recognizes single-line and multi-line comments (e.g., `//` or `#`), categorizing them accurately based on the target language.
* **Identifiers:** Captures custom variable and function names following standard language naming constraints (e.g., alphanumeric strings starting with a letter).
* **Keywords & Operators:** Maps pre-defined language keywords (like `if`, `while`, `return`) and recognized arithmetic/assignment operators.
* **Numbers:** Identifies numeric literals and extracts them as distinct token units.

---

## 🏗️ Architecture & Data Structures
The Lexer relies on deterministic structural pipelines to parse characters safely and map elements:

1. **Character Stream Engine:** Reads input code sequentially, evaluating characters one by one to determine word boundaries.
2. **Token Stream Generator:** Outputs an ordered collection of processed chunks encapsulated as `Token` data objects. Each token maintains its literal value and lexical type (e.g., `[KEYWORD] -> val`).
3. **Symbol Table:** An optimized dynamic data set tracking all unique identifiers discovered in the code. This mimics how a real compiler tracks variables for subsequent syntax analysis and memory allocation stages.

---

## 🛠️ Technical Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Environment:** JDK 11 or higher
* **Build System:** Gradle

---

## 📱 Mobile Testing (Android APK)

If you want to test the Lexer on the go without setting up a desktop development environment, you can install the Android build directly to your phone:

1. **Download** the latest `.apk` file from the [Releases](https://github.com/kamyab9k/LexicalAnalyzer/releases/download/v1.0/Lexer.apk) section of this repository.
2. **Install** it on your Android device *(Note: You may need to ensure "Install from Unknown Sources" is enabled in your device settings).*
3. **Run it:** Open the app, type or paste a custom code snippet into the input field, and tap **Analyze** to watch the live Token Stream and Symbol Table generate right on your phone!
