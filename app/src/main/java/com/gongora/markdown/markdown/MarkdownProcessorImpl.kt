package com.gongora.markdown.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class MarkdownProcessorImpl : MarkdownProcessor {
    override fun parse(markdown: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()

        val tableBlocks = extractMarkdownTableBlocks(markdown)

        if (tableBlocks.isNotEmpty()) {
            tableBlocks.forEach { block ->
                elements.add(parseTable(block))
            }
        } else {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
            elements.addAll(parseMarkdownToElements(parsedTree, markdown))
        }

        return elements
    }

    private fun extractMarkdownTableBlocks(markdown: String): List<String> {
        val lines = markdown.lines()
        val blocks = mutableListOf<String>()
        val buffer = mutableListOf<String>()

        for (line in lines) {
            if (line.trim().startsWith("|")) {
                buffer.add(line)
            } else if (buffer.isNotEmpty()) {
                blocks.add(buffer.joinToString("\n"))
                buffer.clear()
            }
        }

        if (buffer.isNotEmpty()) {
            blocks.add(buffer.joinToString("\n"))
        }

        return blocks.filter { it.contains("---") } // Confirmamos que tiene línea de separador
    }



    private fun parseMarkdownToElements(node: ASTNode, markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.ATX_1 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 1))
                MarkdownElementTypes.ATX_2 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 2))
                MarkdownElementTypes.ATX_3 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 3))

                MarkdownElementTypes.PARAGRAPH -> elements.addAll(parseParagraph(child, markdownText))

                MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> elements.add(
                    parseListElement(child, markdownText, child.type == MarkdownElementTypes.ORDERED_LIST)
                )

                MarkdownElementTypes.LIST_ITEM -> elements.add(MarkdownElement.ListItem(extractText(child, markdownText), false))

                MarkdownElementTypes.CODE_BLOCK -> elements.add(MarkdownElement.CodeBlock(extractText(child, markdownText)))
                MarkdownElementTypes.BLOCK_QUOTE -> elements.add(MarkdownElement.Quote(extractText(child, markdownText)))

                else -> elements.addAll(parseMarkdownToElements(child, markdownText))
            }
        }

        return elements
    }

    private fun extractText(node: ASTNode, markdownText: String): String {
        val builder = StringBuilder()

        fun recurse(current: ASTNode) {
            if (current.type == MarkdownTokenTypes.TEXT) {
                builder.append(current.getTextInNode(markdownText))
                builder.append(" ")
            } else {
                current.children.forEach { recurse(it) }
            }
        }

        recurse(node)
        return builder.toString().trim()
    }

    private fun parseParagraph(node: ASTNode, markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        val paragraphText = extractText(node, markdownText)

        if (isMarkdownTable(paragraphText)) {
            elements.add(parseTable(paragraphText))
        } else {
            val inlineElements = parseInlineElements(node, markdownText)
            val currentParagraph = mutableListOf<MarkdownElement.InlineElement>()

            inlineElements.forEach { element ->
                when (element) {
                    is MarkdownElement.InlineElement.Image -> {
                        if (currentParagraph.isNotEmpty()) {
                            elements.add(MarkdownElement.RichParagraph(currentParagraph.toList()))
                            currentParagraph.clear()
                        }
                        elements.add(MarkdownElement.Image(element.altText, element.url))
                    }
                    else -> currentParagraph.add(element)
                }
            }

            if (currentParagraph.isNotEmpty()) {
                elements.add(MarkdownElement.RichParagraph(currentParagraph.toList()))
            }
        }

        return elements
    }

    private fun parseInlineElements(node: ASTNode, markdownText: String): List<MarkdownElement.InlineElement> {
        val elements = mutableListOf<MarkdownElement.InlineElement>()

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.EMPH -> elements.add(
                    MarkdownElement.InlineElement.Italic(extractText(child, markdownText))
                )
                MarkdownElementTypes.STRONG -> elements.add(
                    MarkdownElement.InlineElement.Bold(extractText(child, markdownText))
                )
                MarkdownElementTypes.INLINE_LINK -> {
                    val isImage = child.startOffset > 0 && markdownText[child.startOffset - 1] == '!'
                    if (isImage) {
                        elements.add(parseInlineImageElement(child, markdownText))
                    } else {
                        elements.add(parseInlineLinkElement(child, markdownText))
                    }
                }
                MarkdownTokenTypes.TEXT -> elements.add(
                    MarkdownElement.InlineElement.Text(child.getTextInNode(markdownText).toString())
                )
                else -> elements.addAll(parseInlineElements(child, markdownText))
            }
        }

        return elements
    }

    private fun parseInlineLinkElement(node: ASTNode, markdownText: String): MarkdownElement.InlineElement.Link {
        var label = ""
        var destination = ""

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.LINK_TEXT -> label = extractText(child, markdownText)
                MarkdownElementTypes.LINK_DESTINATION -> destination = extractText(child, markdownText)
            }
        }

        return MarkdownElement.InlineElement.Link(label, destination)
    }

    private fun parseInlineImageElement(node: ASTNode, markdownText: String): MarkdownElement.InlineElement.Image {
        var altText = ""
        var url = ""

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.LINK_TEXT -> altText = extractText(child, markdownText)
                MarkdownElementTypes.LINK_DESTINATION -> url = extractText(child, markdownText)
            }
        }

        return MarkdownElement.InlineElement.Image(altText, url)
    }

    private fun parseListElement(node: ASTNode, markdownText: String, ordered: Boolean): MarkdownElement.List {
        val items = node.children
            .filter { it.type == MarkdownElementTypes.LIST_ITEM }
            .map { MarkdownElement.ListItem(extractText(it, markdownText), ordered) }

        return MarkdownElement.List(items, ordered)
    }

    private fun isMarkdownTable(text: String): Boolean {
        val lines = text.trim().lines()
            .filter { it.trim().isNotEmpty() }

        if (lines.size < 2) return false
        if (!lines[0].trim().startsWith("|")) return false
        if (!lines[1].contains("-")) return false

        // Verifica que la segunda línea tenga al menos un separador de columna bien formado
        val pattern = Regex("""^\s*\|?\s*-+\s*(\|\s*-+\s*)+\|?\s*$""")
        return pattern.matches(lines[1].trim())
    }


    private fun parseTable(tableText: String): MarkdownElement.Table {
        val lines = tableText.trim().lines()
            .filter { it.trim().isNotEmpty() }

        val headers = lines.first().split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val rows = lines.drop(2).map { row ->
            row.split("|")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

        return MarkdownElement.Table(headers, rows)
    }

}