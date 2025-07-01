# Markdown Parser Library

This repository now includes a reusable Kotlin library for parsing Markdown documents.
The library lives in the `markdownparser` module and exposes a simple API:

```kotlin
interface MarkdownProcessor {
    fun parse(markdown: String): List<MarkdownElement>
}
```

`MarkdownProcessorImpl` provides a concrete implementation based on JetBrains Markdown.

To use the library from another project, include the `markdownparser` module as a dependency
and call `MarkdownProcessorImpl().parse()` with your Markdown string.

The module also offers Jetpack Compose components for rendering the parsed elements.
Use `Markdown` with an optional `MarkdownComponentFactory` to customize how each element is displayed:

```kotlin
val elements = MarkdownProcessorImpl().parse(markdownText)
Markdown(elements)
```

## Sample Module

The `sample` module provides a small CLI application demonstrating how to use
`MarkdownProcessorImpl`. Run it with:

```bash
./gradlew :sample:run
```
