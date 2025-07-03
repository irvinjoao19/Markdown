package com.gongora.markdown.markdown

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun DefaultHeader(text: String, level: Int) {
    Text(
        text = text,
        fontSize = when (level) {
            1 -> 32.sp
            2 -> 28.sp
            3 -> 26.sp
            4 -> 20.sp
            5 -> 16.sp
            else -> 12.sp
        },
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun Markdown(
    markdown: String,
    modifier: Modifier = Modifier,
    header: @Composable (text: String, level: Int) -> Unit = { text, level ->
        DefaultHeader(text, level)
    },
    image: @Composable (alt: String, url: String) -> Unit = { alt, url ->
        DefaultImage(MarkdownElement.Image(alt, url))
    },
    table: @Composable (
        headers: List<String>,
        rows: List<List<String>>,
        modifier: Modifier
    ) -> Unit = { h, r, m ->
        DefaultTable(h, r, m)
    }
) {
    val elements = remember(markdown) { MarkdownParser.parse(markdown) }
    RenderMarkdown(elements, modifier, header, image, table)
}

@Composable
private fun RenderMarkdown(
    elements: List<MarkdownElement>,
    modifier: Modifier,
    header: @Composable (text: String, level: Int) -> Unit,
    image: @Composable (alt: String, url: String) -> Unit,
    table: @Composable (headers: List<String>, rows: List<List<String>>, modifier: Modifier) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Header -> header(element.text, element.level)

                is MarkdownElement.RichParagraph -> RenderRichParagraph(element)

                is MarkdownElement.ListItem -> Text(
                    text = "• ${element.text}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                )

                is MarkdownElement.List -> {
                    element.items.forEachIndexed { index, item ->
                        val prefix = if (element.ordered) "${index + 1}. " else "• "
                        Text(
                            text = "$prefix${item.text}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                        )
                    }
                }

                is MarkdownElement.CodeBlock -> Text(
                    text = element.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(8.dp)
                )

                is MarkdownElement.Quote -> Text(
                    text = element.text,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                is MarkdownElement.Image -> image(element.altText, element.url)

                is MarkdownElement.Table -> table(
                    element.headers,
                    element.rows,
                    modifier,
                )
            }
        }
    }
}

@Composable
fun DefaultImage(element: MarkdownElement.Image) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .crossfade(true)
        .build()

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(element.url)
            .crossfade(true)
            .build(),
        contentDescription = element.altText,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        imageLoader = imageLoader
    )
}


@Composable
fun RenderRichParagraph(element: MarkdownElement.RichParagraph) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        val textBuilder = AnnotatedString.Builder()
        val linkMap = mutableListOf<Pair<IntRange, String>>()

        element.elements.forEach { inlineElement ->
            when (inlineElement) {
                is MarkdownElement.InlineElement.Text -> {
                    textBuilder.append("${inlineElement.text} ")
                }

                is MarkdownElement.InlineElement.Bold -> {
                    val start = textBuilder.length
                    textBuilder.append("${inlineElement.text} ")
                    textBuilder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start,
                        textBuilder.length
                    )
                }

                is MarkdownElement.InlineElement.Italic -> {
                    val start = textBuilder.length
                    textBuilder.append("${inlineElement.text} ")
                    textBuilder.addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        start,
                        textBuilder.length
                    )
                }

                is MarkdownElement.InlineElement.Link -> {
                    val start = textBuilder.length
                    textBuilder.append("${inlineElement.text} ")
                    textBuilder.addStyle(
                        SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline
                        ), start, textBuilder.length
                    )
                    linkMap.add(start until textBuilder.length to inlineElement.url)
                }

                is MarkdownElement.InlineElement.Image -> {
                    // Renderizar el texto acumulado antes de la imagen
                    if (textBuilder.length > 0) {
                        ClickableText(
                            text = textBuilder.toAnnotatedString(),
                            onClick = { offset ->
                                linkMap.forEach { (range, url) ->
                                    if (offset in range) {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Renderizar la imagen inline
                    AsyncImage(
                        model = inlineElement.url,
                        contentDescription = inlineElement.altText,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        // Renderizar el texto restante
        if (textBuilder.length > 0) {
            ClickableText(
                text = textBuilder.toAnnotatedString(),
                onClick = { offset ->
                    linkMap.forEach { (range, url) ->
                        if (offset in range) {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DefaultTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    headerStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
    cellPadding: Dp = 12.dp,
    borderColor: Color = Color(0xFFE0E0E0),
    maxColumnWidth: Dp = 200.dp // Nuevo parámetro para limitar el ancho máximo
) {
    val scrollState = rememberScrollState()
    val columnWidths = remember { mutableStateMapOf<Int, Dp>() }

    Column(modifier = modifier.fillMaxWidth()) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(scrollState)
        ) {
            headers.forEachIndexed { index, header ->
                Text(
                    text = header,
                    style = headerStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .width(columnWidths[index] ?: maxColumnWidth)
                        .padding(cellPadding)
//                    modifier = Modifier
//                        .widthIn(max = maxColumnWidth) // Limitar ancho máximo
//                        .padding(cellPadding)
//                        .onGloballyPositioned { coordinates ->
//                            val width = min(
//                                coordinates.size.width.dp + cellPadding * 2,
//                                maxColumnWidth
//                            )
//                            columnWidths[index] = width
//                        }
                )
            }
        }

        HorizontalDivider(color = borderColor,modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        row.forEachIndexed { index, cell ->
                            Text(
                                text = cell,
                                style = textStyle,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(columnWidths[index] ?: maxColumnWidth)
                                    .padding(cellPadding)
                            )
                        }
                    }
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}


