package com.gongora.markdown.markdown

interface MarkdownProcessor {

    fun parse(markdown: String): List<MarkdownElement>
}