package com.gongora.markdown.markdown

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import coil.ImageLoader
import coil.request.ImageRequest

@Composable
fun MarkdownScreen(viewModel: MarkdownViewModel, modifier: Modifier) {
    RenderMarkdown(viewModel.elements,modifier)
}

@Composable
fun RenderMarkdown(elements: List<MarkdownElement>, modifier: Modifier) {

    Column(modifier = modifier.padding(16.dp)) {
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Header -> Text(
                    text = element.text,
                    fontSize = when (element.level) {
                        1 -> 32.sp
                        2 -> 24.sp
                        else -> 20.sp
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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

                is MarkdownElement.Image -> RenderImage(element)

                is MarkdownElement.Table -> RenderTable(element.headers, element.rows)
            }
        }
    }
}

@Composable
fun RenderImage(element: MarkdownElement.Image) {
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
fun RenderTable(header: List<String>, body: List<List<String>>, color : Color = Color.Black) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            Row {
                header.forEach {
                    Text(it)
                }
            }

            body.forEach { rows->
                Row  {
                    rows.forEach {
                        Text(text = it,modifier = Modifier.padding())
                    }
                }
            }
        }
    }
}