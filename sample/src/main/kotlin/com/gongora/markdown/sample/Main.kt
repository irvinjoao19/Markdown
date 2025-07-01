package com.gongora.markdown.sample

import com.gongora.markdown.parser.MarkdownProcessorImpl

fun main() {
    val markdown = """
        # Sample Heading

        This is a **sample** markdown with a link [Google](https://google.com).
    """.trimIndent()

    val processor = MarkdownProcessorImpl()
    val elements = processor.parse(markdown)
    println(elements.joinToString("\n"))
}
