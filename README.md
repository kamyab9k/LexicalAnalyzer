# Lexical Analyzer (Lexer) as a mobile application

A lightweight, handcrafted Lexical Analyzer (Lexer) implemented as a mobile application. This project was developed as a fundamental language engineering task to process source code sequentially in a character-based manner, converting raw code text into a structured stream of tokens while building a dynamically populated symbol table.

## 🎯 Learning Goal & Objective
The primary core goal of this project is to implement core lexical translation tasks manually to deeply understand the state transitions, pattern matching, and tokenization mechanics that underpin automated scanner tools (like Lex/Flex). 

### Language Features Handled
The custom Lexer processes a targeted, representative subset of programming syntax containing:
*   **Comments:** Single-line and multi-line comments (skipped or explicitly logged based on processing parameters).
*   **Identifiers:** Custom variable and function names following standard language naming constraints (e.g., alphanumeric starting with a letter).
*   **Keywords & Operators:** Pre-defined language keywords and recognized arithmetic/assignment operators.

---

## 🏗️ Architecture & Data Structures

The Lexer relies on deterministic structural pipelines to parse characters safely and map elements:

1. **Character Stream Engine:** Reads input files sequentially, tracking exact line and column positions for accurate metadata logging.
2. **Token Stream Generator:** Outputs an ordered collection of processed chunks encapsulated as `Token` data objects. Each token maintains its literal value, lexical type, and positional metadata.
3. **Symbol Table:** An optimized dynamic data map tracking all discovered identifiers and context metrics for subsequent compilation stages.

---

## 🛠️ Technical Stack & Setup

*   **Language:** Kotlin
*   **Environment:** JDK 11 or higher
*   **Build System:** Gradle (or standalone Kotlin Compiler)


### 📱 Mobile Testing (Android APK)

If you want to test the Lexer on the go without setting up a desktop development environment, you can install the Android build directly:

1. Download the latest `.apk` file from the [Releases](https://github.com/kamyab9k/LexicalAnalyzer/releases/tag/v1.0) section of this repository.
2. Install it on your Android device (ensure "Install from Unknown Sources" is enabled in your settings).
3. Open the app, paste any custom code snippet you want into the input field, and tap **Analyze** to view the live Token Stream and Symbol Table right on your phone!
