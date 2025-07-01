package com.gongora.markdown.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gongora.markdown.parser.MarkdownElement

class DefaultMarkdownComponentFactory : MarkdownComponentFactory {

    @Composable
    override fun Header(text: String, level: Int) {
        val style = when (level) {
            1 -> MaterialTheme.typography.h4
            2 -> MaterialTheme.typography.h5
            3 -> MaterialTheme.typography.h6
            else -> MaterialTheme.typography.subtitle1
        }
        Text(text = text, style = style, modifier = Modifier.padding(vertical = 4.dp))
    }

    @Composable
    override fun Paragraph(inlines: List<MarkdownElement.InlineElement>) {
        Text(text = inlines.joinToString("") { inlineToString(it) }, modifier = Modifier.padding(vertical = 2.dp))
    }

    @Composable
    override fun List(items: List<MarkdownElement.ListItem>, ordered: Boolean) {
        Column(modifier = Modifier.padding(start = 8.dp)) {
            items.forEachIndexed { index, item ->
                val prefix = if (ordered) "${index + 1}. " else "\u2022 "
                Text(prefix + item.text)
            }
        }
    }

    @Composable
    override fun CodeBlock(text: String) {
        Text(text = text, style = MaterialTheme.typography.body2, modifier = Modifier.padding(4.dp))
    }

    @Composable
    override fun Quote(text: String) {
        Text(text = text, modifier = Modifier.padding(4.dp))
    }

    @Composable
    override fun Image(altText: String, url: String) {
        Text(text = altText)
    }

    @Composable
    override fun Table(headers: List<String>, rows: List<List<String>>) {
        Column {
            Row {
                headers.forEach { header ->
                    Text(header, modifier = Modifier.weight(1f))
                }
            }
            rows.forEach { row ->
                Row {
                    row.forEach { cell ->
                        Text(cell, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    private fun inlineToString(inline: MarkdownElement.InlineElement): String = when (inline) {
        is MarkdownElement.InlineElement.Text -> inline.text
        is MarkdownElement.InlineElement.Bold -> inline.text
        is MarkdownElement.InlineElement.Italic -> inline.text
        is MarkdownElement.InlineElement.Link -> inline.text
        is MarkdownElement.InlineElement.Image -> inline.altText
    }
}
