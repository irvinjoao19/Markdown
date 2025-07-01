package com.gongora.markdown.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.gongora.markdown.parser.MarkdownElement

@Composable
fun Markdown(elements: List<MarkdownElement>, factory: MarkdownComponentFactory = DefaultMarkdownComponentFactory()) {
    Column {
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Header -> factory.Header(element.text, element.level)
                is MarkdownElement.RichParagraph -> factory.Paragraph(element.elements)
                is MarkdownElement.List -> factory.List(element.items, element.ordered)
                is MarkdownElement.CodeBlock -> factory.CodeBlock(element.text)
                is MarkdownElement.Quote -> factory.Quote(element.text)
                is MarkdownElement.Image -> factory.Image(element.altText, element.url)
                is MarkdownElement.Table -> factory.Table(element.headers, element.rows)
                is MarkdownElement.ListItem -> { /* Handled inside MarkdownElement.List */ }
            }
        }
    }
}
