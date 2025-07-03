# Markdown Library

This repository contains a simple Jetpack Compose library for rendering Markdown text.
The `markdownlib` module exposes a `Markdown` composable that parses Markdown
strings and displays them using Compose.

## Adding the dependency

If you are using this repository as a multi-module project, add the library
module as a dependency:

```kotlin
implementation(project(":markdownlib"))
```

## Basic usage

Use the `Markdown` composable by providing the Markdown text:

```kotlin
Markdown(markdown = "# Hello\nThis is **Markdown**")
```

The component will render headers, paragraphs, images and tables with default
styles.

## Customising the appearance

`Markdown` accepts optional lambdas so you can replace the default header, image
and table implementations:

```kotlin
Markdown(
    markdown = content,
    header = { text, level ->
        // Custom header composable
        Text(text, fontSize = 20.sp)
    },
    image = { alt, url ->
        // Custom image rendering
        AsyncImage(model = url, contentDescription = alt)
    },
    table = { headers, rows, modifier ->
        // Custom table implementation
        MyTable(headers, rows, modifier)
    }
)
```

By overriding these slots you can easily integrate the parser with your own
components.
