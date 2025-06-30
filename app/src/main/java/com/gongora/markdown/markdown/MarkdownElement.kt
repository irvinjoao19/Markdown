package com.gongora.markdown.markdown

sealed class MarkdownElement {
    data class Header(val text: String, val level: Int) : MarkdownElement()
    data class RichParagraph(val elements: kotlin.collections.List<InlineElement>) :
        MarkdownElement()

    data class ListItem(val text: String, val ordered: Boolean) : MarkdownElement()
    data class List(val items: kotlin.collections.List<ListItem>, val ordered: Boolean) :
        MarkdownElement()

    data class CodeBlock(val text: String) : MarkdownElement()
    data class Quote(val text: String) : MarkdownElement()
    data class Image(val altText: String, val url: String) : MarkdownElement()

    data class Table(
        val headers: kotlin.collections.List<String>,
        val rows: kotlin.collections.List<kotlin.collections.List<String>>
    ) : MarkdownElement()


    sealed class InlineElement {
        data class Text(val text: String) : InlineElement()
        data class Bold(val text: String) : InlineElement()
        data class Italic(val text: String) : InlineElement()
        data class Link(val text: String, val url: String) : InlineElement()
        data class Image(val altText: String, val url: String) : InlineElement()
    }
}
