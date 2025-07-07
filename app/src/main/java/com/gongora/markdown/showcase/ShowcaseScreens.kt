package com.gongora.markdown.showcase

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.gongora.markdown.markdown.Markdown

@Composable
fun MarkdownInputScreen(onPreview: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Editor Markdown") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                placeholder = { Text("Ingrese markdown...") }
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onPreview(text.text) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Visualizar")
            }
        }
    }
}

@Composable
fun MarkdownPreviewScreen(markdown: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Vista Previa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Markdown(
            markdown = markdown,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
