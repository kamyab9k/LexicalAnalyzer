package com.example.modelbasedsoftwareengineering

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.modelbasedsoftwareengineering.ui.theme.ModelBasedSoftwareEngineeringTheme
import kotlinx.coroutines.delay

// --- LANGUAGE CONFIGURATION ---

enum class CodeLanguage(
    val displayName: String,
    val commentPrefix: String,
    val keywords: Set<String>
) {
    KOTLIN(
        "Kotlin", "//",
        setOf("val", "var", "fun", "class", "if", "else", "for", "while", "return", "true", "false", "null", "import", "package")
    ),
    JAVA(
        "Java", "//",
        setOf("int", "boolean", "String", "class", "public", "private", "static", "void", "if", "else", "for", "while", "return", "true", "false", "null", "import", "package", "new")
    ),
    PYTHON(
        "Python", "#",
        setOf("def", "class", "if", "else", "elif", "for", "while", "return", "True", "False", "None", "import", "from", "and", "or", "not", "pass")
    ),
    CPP(
        "C++", "//",
        setOf("int", "bool", "class", "public", "private", "void", "if", "else", "for", "while", "return", "true", "false", "nullptr", "include", "using", "namespace")
    )
}

// --- LEXER LOGIC ---

data class Token(val type: String, val value: String)

class CompleteLexer(private val input: String, private val language: CodeLanguage) {
    private var pos = 0
    private val tokenStream = mutableListOf<Token>()
    private val symbolTable = mutableSetOf<String>()

    private fun peek(): Char {
        return if (pos + 1 < input.length) input[pos + 1] else '\u0000'
    }

    fun analyze() {
        while (pos < input.length) {
            val char = input[pos]
            when {
                char.isWhitespace() -> pos++

                // Comments (Handles both // for C-style and # for Python)
                (language.commentPrefix == "//" && char == '/' && peek() == '/') ||
                        (language.commentPrefix == "#" && char == '#') -> {
                    val start = pos
                    while (pos < input.length && input[pos] != '\n') pos++
                    tokenStream.add(Token("COMMENT", input.substring(start, pos)))
                }

                // Words (Identifiers & Keywords)
                char.isLetter() || char == '_' -> {
                    val start = pos
                    while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
                    val word = input.substring(start, pos)
                    if (word in language.keywords) {
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
                char in "+=-*/(){}[],.<>!&|" -> {
                    tokenStream.add(Token("SYMBOL", char.toString()))
                    pos++
                }

                else -> {
                    tokenStream.add(Token("UNKNOWN", char.toString()))
                    pos++
                }
            }
        }

        Log.d("LexerResult", "--- NEW ANALYSIS START (${language.displayName}) ---")
        tokenStream.forEach { Log.d("LexerResult", "Token: [${it.type}] Value: ${it.value}") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexerApp() {
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.KOTLIN) }
    var codeInput by remember { mutableStateOf("val x = 10\nif (x > 5) {\n  return x\n}") }

    // State for the animation
    var fullResultText by remember { mutableStateOf("") }
    var animatedResultText by remember { mutableStateOf("Results will appear here...") }

    // Typewriter Animation Effect
    LaunchedEffect(fullResultText) {
        if (fullResultText.isNotEmpty()) {
            animatedResultText = ""
            // Reveal text a few characters at a time to create a fast terminal effect
            val chunkSize = 3
            for (i in 0..fullResultText.length step chunkSize) {
                animatedResultText = fullResultText.take(i)
                delay(1) // 1ms delay for a smooth, fast rolling animation
            }
            animatedResultText = fullResultText // ensure exact match at the end
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Language Lexer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LanguageDropdown(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = {
                    selectedLanguage = it
                    // Optional: update dummy text based on language selected
                    codeInput = when(it) {
                        CodeLanguage.KOTLIN -> "val x = 10\n// Kotlin code\nif (x > 5) {\n  return x\n}"
                        CodeLanguage.JAVA -> "int x = 10;\n// Java code\nif (x > 5) {\n  return x;\n}"
                        CodeLanguage.PYTHON -> "x = 10\n# Python code\nif x > 5:\n  return x"
                        CodeLanguage.CPP -> "int x = 10;\n// C++ code\nif (x > 5) {\n  return x;\n}"
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Input Code:", style = MaterialTheme.typography.titleMedium)

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
                    val lexer = CompleteLexer(codeInput, selectedLanguage)
                    lexer.analyze()
                    // Set full string. The LaunchedEffect will catch this and animate it.
                    fullResultText = lexer.getOutput()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run ${selectedLanguage.displayName} Lexical Analysis")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Output:", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = animatedResultText,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    selectedLanguage: CodeLanguage,
    onLanguageSelected: (CodeLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLanguage.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CodeLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}