plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.gongora.markdown.sample.MainKt")
}

dependencies {
    implementation(project(":markdownparser"))
}
