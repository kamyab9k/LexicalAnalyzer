package com.example.modelbasedsoftwareengineering

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import com.example.modelbasedsoftwareengineering.ui.theme.ModelBasedSoftwareEngineeringTheme

// --- LEXER LOGIC ---

data class Token(val type: String, val value: String)

class CompleteLexer(private val input: String) {
    private var pos = 0
    private val tokenStream = mutableListOf<Token>()
    private val symbolTable = mutableSetOf<String>()

    // Complete Keyword List
    private val keywords = setOf(
        "val", "var", "fun", "class", "if", "else", "for", "while",
        "return", "true", "false", "null", "import", "package"
    )

    // Helper function to see next character in the input
    private fun peek(): Char {
        // If we are at the last character, return a 'null' character \u0000
        // to avoid index out of bounds errors.
        return if (pos + 1 < input.length) input[pos + 1] else '\u0000'
    }

    fun analyze() {
        while (pos < input.length) {
            val char = input[pos]
            when {
                char.isWhitespace() -> pos++

                // Comments
                char == '/' && peek() == '/' -> {
                    val start = pos
                    while (pos < input.length && input[pos] != '\n') pos++
                    tokenStream.add(Token("COMMENT", input.substring(start, pos)))
                }

                // Words
                char.isLetter() || char == '_' -> {
                    val start = pos
                    while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
                    val word = input.substring(start, pos)
                    if (word in keywords) {
                        tokenStream.add(Token("KEYWORD", word))
                    } else {
                        tokenStream.add(Token("IDENTIFIER", word))
                        symbolTable.add(word)
                    }
                }

                // Numbers
                char.isDigit() -> {
                    val start = pos
                    while (pos < input.length && input[pos].isDigit()) pos++
                    tokenStream.add(Token("NUMBER", input.substring(start, pos)))
                }

                // Symbols
                char in "+=-*/(){}[],." -> {
                    tokenStream.add(Token("SYMBOL", char.toString()))
                    pos++
                }

                else -> {
                    tokenStream.add(Token("UNKNOWN", char.toString()))
                    pos++
                }
            }
        }

        // --- LOGGING SECTION ---
        // This prints to the 'Logcat' in Android Studio
        Log.d("LexerResult", "--- NEW ANALYSIS START ---")
        tokenStream.forEach { Log.d("LexerResult", "Token: [${it.type}] Value: ${it.value}") }
        Log.d("LexerResult", "Symbol Table: $symbolTable")
    }

    fun getOutput(): String {
        val streamText = tokenStream.joinToString("\n") { "[${it.type.padEnd(10)}] -> ${it.value}" }
        val tableText = symbolTable.joinToString(", ")
        return "TOKEN STREAM:\n$streamText\n\nSYMBOL TABLE:\n$tableText"
    }
}

// --- UI SECTION ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModelBasedSoftwareEngineeringTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LexerApp()
                }
            }
        }
    }
}

@Composable
fun LexerApp() {
    // State variables to hold the text input and the result
    var codeInput by remember { mutableStateOf("val x = 10\nif (x > 5) {\n  return x\n}") }
    var resultText by remember { mutableStateOf("Results will appear here...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 40.dp)
    ) {
        Text("Input Code:", style = MaterialTheme.typography.titleMedium)

        // The "EditText" equivalent in Jetpack Compose
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Enter code here...") },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val lexer = CompleteLexer(codeInput)
                lexer.analyze()
                resultText = lexer.getOutput()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Run Lexical Analysis")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Output:", style = MaterialTheme.typography.titleMedium)

        // Display area for the results
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = resultText,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}