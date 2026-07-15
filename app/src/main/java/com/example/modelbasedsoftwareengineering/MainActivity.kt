package com.example.modelbasedsoftwareengineering

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modelbasedsoftwareengineering.ui.theme.ModelBasedSoftwareEngineeringTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private fun peek(): Char {
        return if (pos + 1 < input.length) input[pos + 1] else '\u0000'
    }

    fun analyze(): List<Token> {
        while (pos < input.length) {
            val char = input[pos]
            when {
                char.isWhitespace() -> pos++

                // Comments
                (language.commentPrefix == "//" && char == '/' && peek() == '/') ||
                        (language.commentPrefix == "#" && char == '#') -> {
                    val start = pos
                    while (pos < input.length && input[pos] != '\n') pos++
                    tokenStream.add(Token("COMMENT", input.substring(start, pos)))
                }

                // Words
                char.isLetter() || char == '_' -> {
                    val start = pos
                    while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
                    val word = input.substring(start, pos)
                    if (word in language.keywords) {
                        tokenStream.add(Token("KEYWORD", word))
                    } else {
                        tokenStream.add(Token("IDENTIFIER", word))
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
        return tokenStream
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

    // State for the step-by-step execution
    val visibleTokens = remember { mutableStateListOf<Token>() }
    val symbolTable = remember { mutableStateListOf<String>() }
    var isRunning by remember { mutableStateOf(false) }

    // State for the flying animation
    var flyingSymbol by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lexer") },
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
                    codeInput = when (it) {
                        CodeLanguage.KOTLIN -> "val score = 100\nif (score > 50) {\n  return score\n}"
                        CodeLanguage.JAVA -> "int score = 100;\nif (score > 50) {\n  return score;\n}"
                        CodeLanguage.PYTHON -> "score = 100\nif score > 50:\n  return score"
                        CodeLanguage.CPP -> "int score = 100;\nif (score > 50) {\n  return score;\n}"
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Enter code here...") },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                enabled = !isRunning
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isRunning) return@Button

                    coroutineScope.launch {
                        isRunning = true
                        visibleTokens.clear()
                        symbolTable.clear()
                        flyingSymbol = null

                        val lexer = CompleteLexer(codeInput, selectedLanguage)
                        val allTokens = lexer.analyze()

                        // Process tokens step-by-step
                        for (token in allTokens) {
                            visibleTokens.add(token)

                            // If it's a new identifier, trigger the animation
                            if (token.type == "IDENTIFIER" && !symbolTable.contains(token.value)) {
                                flyingSymbol = token.value
                                delay(600) // Wait for animation to finish moving across screen
                                symbolTable.add(token.value)
                                flyingSymbol = null
                            } else {
                                delay(100) // Small delay for normal tokens
                            }
                        }
                        isRunning = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning
            ) {
                Text(if (isRunning) "Analyzing..." else "Run ${selectedLanguage.displayName} Lexical Analysis")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split the bottom area into two columns for Token Stream and Symbol Table
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT COLUMN: Token Stream
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                    ) {
                        Text("Token Stream", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        val listState = rememberLazyListState()
                        // Auto-scroll to bottom as items are added
                        LaunchedEffect(visibleTokens.size) {
                            if (visibleTokens.isNotEmpty()) {
                                listState.animateScrollToItem(visibleTokens.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            items(visibleTokens) { token ->
                                TokenChip(token)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // RIGHT COLUMN: Symbol Table
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                    ) {
                        Text("Symbol Table", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            items(symbolTable) { symbol ->
                                SymbolChip(symbol)
                            }
                        }
                    }
                }

                // The Flying Animation Overlay
                if (flyingSymbol != null) {
                    FlyingTokenAnimation(symbol = flyingSymbol!!)
                }
            }
        }
    }
}

@Composable
fun FlyingTokenAnimation(symbol: String) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Animation progress from 0f to 1f
    val progress = remember { Animatable(0f) }

    LaunchedEffect(symbol) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    // Calculate position: Starts on the left (Token Stream), moves to the right (Symbol Table)
    // We use lerp mathematically based on fraction
    val xOffset = (screenWidth * 0.1f) + ((screenWidth * 0.4f) * progress.value)

    // Arc movement: slightly goes up then down
    val yOffset = if (progress.value < 0.5f) {
        100.dp - (50.dp * (progress.value * 2))
    } else {
        50.dp + (50.dp * ((progress.value - 0.5f) * 2))
    }

    // Fade out at the very end as it "lands"
    val alpha = if (progress.value > 0.8f) (1f - progress.value) * 5 else 1f

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .alpha(alpha)
    ) {
        SymbolChip(symbol = symbol, isFlying = true)
    }
}

@Composable
fun TokenChip(token: Token) {
    val backgroundColor = when (token.type) {
        "KEYWORD" -> Color(0xFF1E88E5) // Blue
        "IDENTIFIER" -> Color(0xFF43A047) // Green
        "NUMBER" -> Color(0xFFE53935) // Red
        "COMMENT" -> Color(0xFF757575) // Gray
        else -> Color(0xFF8E24AA) // Purple for Symbols/Unknown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor.copy(alpha = 0.15f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[${token.type}]",
            fontWeight = FontWeight.Bold,
            color = backgroundColor,
            fontSize = 12.sp,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = token.value,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SymbolChip(symbol: String, isFlying: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth(if (isFlying) 0.5f else 1f)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFlying) Color(0xFF43A047) else Color(0xFF43A047).copy(alpha = 0.2f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isFlying) Color.White else Color(0xFF2E7D32)
        )
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
            label = { Text("Target Language") },
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