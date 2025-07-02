package com.gongora.markdown.markdown

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MarkdownViewModel(private val processor: MarkdownProcessor) : ViewModel() {

    var elements by mutableStateOf<List<MarkdownElement>>(emptyList())
        private set

    fun loadMarkdown(markdown: String) {
        elements = processor.parse(markdown)
    }
}



