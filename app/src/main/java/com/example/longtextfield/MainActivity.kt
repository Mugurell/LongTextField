package com.example.longtextfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.longtextfield.ui.theme.LongTextFieldTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LongTextFieldTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("BasicTextField cannot render long text")
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textFieldState = rememberTextFieldState()
                        val scrollState = rememberScrollState()
                        var layoutInfo by remember { mutableStateOf("No layout yet.") }

                        BasicTextField(
                            state = textFieldState,
                            scrollState = scrollState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            onTextLayout = { getResult ->
                                val r = getResult()
                                val width = r?.size?.width
                                val clamped = if (width == CONSTRAINTS_MAX) " CLAMPED (Constraints max)" else ""
                                layoutInfo = "len=${textFieldState.text.length}\n" +
                                    "width=${width}px$clamped\n" +
                                    "height=${r?.size?.height}px  lineCount=${r?.lineCount}\n" +
                                    "overflowW=${r?.didOverflowWidth}  overflowH=${r?.didOverflowHeight}"
                                android.util.Log.i(
                                    "ComposeRepro",
                                    "len=${textFieldState.text.length} size=${r?.size} " +
                                        "lineCount=${r?.lineCount} " +
                                        "overflowW=${r?.didOverflowWidth} overflowH=${r?.didOverflowHeight}",
                                )
                            },
                            decorator = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    if (textFieldState.text.isEmpty()) {
                                        Text(
                                            text = "Enter text...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Text(
                            text = "Length: ${textFieldState.text.length}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    textFieldState.setTextAndPlaceCursorAtEnd("A".repeat(100_000))
                                }
                            ) {
                                Text("100k, cursor END")
                            }

                            Button(
                                onClick = {
                                    textFieldState.edit {
                                        replace(0, length, "A".repeat(100_000))
                                        selection = TextRange(0)
                                    }
                                }
                            ) {
                                Text("100k, cursor START")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    textFieldState.edit {
                                        append("A".repeat(5000))
                                        selection = TextRange(length)
                                    }
                                }
                            ) {
                                Text("Add 5k")
                            }

                            Button(
                                onClick = {
                                    textFieldState.edit {
                                        val start = (length - 5000).coerceAtLeast(0)
                                        delete(start, length)
                                        selection = TextRange(length)
                                    }
                                }
                            ) {
                                Text("Remove 5k")
                            }

                            Button(
                                onClick = {
                                    textFieldState.edit {
                                        replace(0, length, "")
                                    }
                                }
                            ) {
                                Text("Clear")
                            }
                        }

                        Text(
                            text = layoutInfo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

// Maximum finite value a Compose Constraints dimension can encode (18-bit focus
// bucket: 0x3FFFF - 1). A measured width pinned here means the layout was clamped.
private const val CONSTRAINTS_MAX = 262142
