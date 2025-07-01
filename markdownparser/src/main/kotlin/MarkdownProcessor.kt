package com.gongora.markdown.parser

interface MarkdownProcessor {

    fun parse(markdown: String): List<MarkdownElement>
}