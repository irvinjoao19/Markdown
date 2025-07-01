package com.gongora.markdown.compose

import androidx.compose.runtime.Composable
import com.gongora.markdown.parser.MarkdownElement

interface MarkdownComponentFactory {
    @Composable
    fun Header(text: String, level: Int)

    @Composable
    fun Paragraph(inlines: List<MarkdownElement.InlineElement>)

    @Composable
    fun List(items: List<MarkdownElement.ListItem>, ordered: Boolean)

    @Composable
    fun CodeBlock(text: String)

    @Composable
    fun Quote(text: String)

    @Composable
    fun Image(altText: String, url: String)

    @Composable
    fun Table(headers: List<String>, rows: List<List<String>>)
}
