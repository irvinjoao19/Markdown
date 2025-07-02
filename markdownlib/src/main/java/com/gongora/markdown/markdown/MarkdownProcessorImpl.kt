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
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)

        // Procesar nodos manteniendo el orden original
        var currentPos = 0
        val tableBlocks = findTableBlocks(markdown)

        tableBlocks.forEach { (start, end, tableText) ->
            // Procesar contenido antes de la tabla
            if (currentPos < start) {
                val nonTableText = markdown.substring(currentPos, start)
                val nonTableTree = MarkdownParser(flavour).buildMarkdownTreeFromString(nonTableText)
                elements.addAll(parseMarkdownToElements(nonTableTree, nonTableText))
            }

            // Procesar la tabla
            if (isMarkdownTable(tableText)) {
                elements.add(parseTable(tableText))
            }

            currentPos = end
        }

        // Procesar contenido después de la última tabla
        if (currentPos < markdown.length) {
            val remainingText = markdown.substring(currentPos)
            val remainingTree = MarkdownParser(flavour).buildMarkdownTreeFromString(remainingText)
            elements.addAll(parseMarkdownToElements(remainingTree, remainingText))
        }

        return elements
    }

    private fun findTableBlocks(markdown: String): List<Triple<Int, Int, String>> {
        val lines = markdown.lines()
        val blocks = mutableListOf<Triple<Int, Int, String>>()
        var tableStart = -1
        var currentPos = 0

        lines.forEachIndexed { index, line ->
            val lineStart = currentPos
            val lineEnd = currentPos + line.length

            if (line.trim().startsWith("|") || (tableStart != -1 && line.trim().contains("|"))) {
                if (tableStart == -1) {
                    tableStart = lineStart
                }
            } else {
                if (tableStart != -1) {
                    // Verificar si el bloque anterior era una tabla válida
                    val tableText = markdown.substring(tableStart, currentPos)
                    if (isMarkdownTable(tableText)) {
                        blocks.add(Triple(tableStart, currentPos, tableText))
                    }
                    tableStart = -1
                }
            }

            currentPos = lineEnd + 1 // +1 para el salto de línea
        }

        // Agregar la última tabla si existe
        if (tableStart != -1) {
            val tableText = markdown.substring(tableStart, currentPos)
            if (isMarkdownTable(tableText)) {
                blocks.add(Triple(tableStart, currentPos, tableText))
            }
        }

        return blocks
    }
    private fun parseMarkdownToElements(node: ASTNode, markdownText: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.ATX_1 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 1))
                MarkdownElementTypes.ATX_2 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 2))
                MarkdownElementTypes.ATX_3 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 3))
                MarkdownElementTypes.ATX_4 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 4))
                MarkdownElementTypes.ATX_5 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 5))
                MarkdownElementTypes.ATX_6 -> elements.add(MarkdownElement.Header(extractText(child, markdownText), 6))
                MarkdownElementTypes.PARAGRAPH -> elements.addAll(parseParagraph(child, markdownText))
                MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST ->
                    elements.add(parseListElement(child, markdownText, child.type == MarkdownElementTypes.ORDERED_LIST))
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
        val rawText = extractText(node, markdownText)
        val elements = mutableListOf<MarkdownElement>()

        // Primero verificar si es una tabla
        if (isMarkdownTable(rawText)) {
            try {
                elements.add(parseTable(rawText))
                // Buscar texto adicional después de la tabla
                val remainingText = rawText.lines()
                    .dropWhile { it.trim().isNotEmpty() && !it.trim().startsWith("|") }
                    .dropWhile { it.trim().isNotEmpty() }
                    .joinToString("\n")

                if (remainingText.isNotBlank()) {
                    elements.addAll(parseParagraph(node, remainingText))
                }
                return elements
            } catch (e: Exception) {
                // Si falla, continuar con el procesamiento normal
            }
        }

        // Procesamiento normal para no tablas
        val inlineElements = parseInlineElements(node, markdownText)
        elements.add(MarkdownElement.RichParagraph(inlineElements))

        return elements
    }

    private fun parseInlineElements(node: ASTNode, markdownText: String): List<MarkdownElement.InlineElement> {
        val elements = mutableListOf<MarkdownElement.InlineElement>()

        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.EMPH ->
                    elements.add(MarkdownElement.InlineElement.Italic(extractText(child, markdownText)))
                MarkdownElementTypes.STRONG ->
                    elements.add(MarkdownElement.InlineElement.Bold(extractText(child, markdownText)))
                MarkdownElementTypes.INLINE_LINK -> {
                    val isImage = child.startOffset > 0 && markdownText[child.startOffset - 1] == '!'
                    if (isImage) {
                        elements.add(parseInlineImageElement(child, markdownText))
                    } else {
                        elements.add(parseInlineLinkElement(child, markdownText))
                    }
                }
                MarkdownTokenTypes.TEXT ->
                    elements.add(MarkdownElement.InlineElement.Text(child.getTextInNode(markdownText).toString()))
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

        // Verificar que al menos la primera línea contenga pipes
        if (!lines[0].contains("|")) return false

        // Verificar que la segunda línea sea un separador válido
        val separatorLine = lines[1].trim()
        val separatorPattern = Regex("""^[\s\|]*[-:]+[\s\|]*[-:\s\|]+$""")
        if (!separatorPattern.matches(separatorLine)) return false

        return true
    }

    private fun parseTable(tableText: String): MarkdownElement.Table {
        val lines = tableText.trim().lines()
            .filter { it.trim().isNotEmpty() }

        // Procesar encabezados (primera línea)
        val headers = lines.first().split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Procesar filas (omitimos la línea de separador)
        val rows = lines.drop(2).mapNotNull { row ->
            val cells = row.split("|")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            // Solo incluir filas que tengan el mismo número de columnas que los encabezados
            if (cells.size == headers.size) cells else null
        }

        return MarkdownElement.Table(headers, rows)
    }
}



