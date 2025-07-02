package com.gongora.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gongora.markdown.markdown.Markdown
import com.gongora.markdown.ui.theme.MarkdownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val markdownSample = """
                        # 💬 BCPInputChat
                        
                        Un componente personalizado de entrada de chat para Android que permite alternar entre entrada de
                        texto y entrada por voz 🎤. Ideal para aplicaciones de mensajería, asistentes virtuales o cualquier
                        interfaz conversacional.
                        
                        ---
                        [<img src="https://i.sstatic.net/IfLtg.png?s=256"/>]()
                        ---
                        
                        ## ✨ Características
                        
                        - 📝 Entrada de texto con botón de envío.
                        - 🎙️ Entrada por voz con botón de micrófono.
                        - 🔄 Cambio dinámico entre modos según el contenido del campo de texto.
                        - ♿ Accesibilidad mejorada.
                        - 🎨 **Personalización** de estilo, hint, íconos y descripciones.
                        
                        ---
                        
                        ## 🧩 Uso
                        
                        ```xml
                        
                        <com.bcp.core.ui.inputchat.BCPInputChat 
                            android:id="@+id/bcpInputChat"
                            android:layout_height="wrap_content" 
                            android:layout_width="match_parent"
                            app:bcp_input_chat_hint="Utiliza el micrófono o teclado" 
                            app:bcp_input_chat_style="mic" />
                        ```
                        
                        ## ⚙️ Atributos
                        
                        #### XML
                        
                        | Atributo                                  | Descripción                                                    | Tipo                             |
                        |-------------------------------------------|----------------------------------------------------------------|----------------------------------|
                        | `bcp_input_chat_hint`                     | Texto de sugerencia en el campo de entrada.                    | String                           |
                        | `bcp_input_chat_style`                    | Estilo inicial: text_only o mic.                               | BCPInputChatStyle: MIC,TEXT_ONLY |
                        | `bcp_input_chat_mic_action_label`         | Etiqueta de acción para accesibilidad del micrófono.           | String                           |
                        | `bcp_input_chat_mic_content_description`  | Descripción del botón del micrófono para lectores de pantalla. | String                           |
                        | `bcp_input_chat_send_action_label`        | Etiqueta de acción para accesibilidad del envío.               | String                           |
                        | `bcp_input_chat_send_content_description` | Descripción del botón del envío para lectores de pantalla.     | String                           |
                        
                        
                        #### Kotlin
                        
                        | Atributo                 | Descripción                                                                                | Tipo                             |
                        |--------------------------|--------------------------------------------------------------------------------------------|----------------------------------|
                        | `text`                   | Permite acceder y modificar directamente el contenido del campo de entrada del componente. | String                           |
                        | `hint`                   | Texto de sugerencia en el campo de entrada.                                                | String                           |
                        | `style`                  | Estilo inicial: text_only o mic.                                                           | BCPInputChatStyle: MIC,TEXT_ONLY |
                        | `micActionLabel`         | Etiqueta de acción para accesibilidad del micrófono.                                       | String                           |
                        | `micContentDescription`  | Descripción del botón del micrófono para lectores de pantalla.                             | String                           |
                        | `sendActionLabel`        | Etiqueta de acción para accesibilidad del envío.                                           | String                           |
                        | `sendContentDescription` | Descripción del botón del envío para lectores de pantalla.                                 | String                           |
                        
                        
                        ## 🧠 Métodos Útiles
                        
                        | Método                             | Descripción                               |
                        |------------------------------------|-------------------------------------------|
                        | setOnMicClickListener {}           | Callback cuando se presiona el micrófono. |
                        | setOnSendCLickListener { text -> } | Callback cuando se presiona enviar.       |
                        | prepareForTalking()                | Cambia de color el micrófono.             |
                        | prepareForTextSend()               | Cambia al modo de solo texto.             |
                        | prepareForVoiceInput()             | Cambia al modo de micrófono.              |
                        | clear()                            | Limpia el campo de texto.                 |
                        
                        
                        
                        ### Accesibilidad
                        
                        El componente `BCPInputChat` tiene soporte para accesibilidad. Cuando se establecen los campos
                        `micActionLabel`,`micContentDescription`,`sendActionLabel`,`sendContentDescription`, estos se utilizan para configurar la descripción de
                        accesibilidad del componente.
                        
                        
                        #### XML
                        
                        ```xml
                        <!-- El campo de texto será anunciado como: "Utiliza el micrófono o teclado, campo de texto,toca dos veces para editar texto." -->
                        <!-- El campo del botón en modo microfono será anunciado como: "Micrófono,botón,toca dos veces para hablar." -->
                        <!-- El campo del botón en modo envío será anunciado como: "Enviar,botón,toca dos veces para enviar mensaje." -->
                        <com.bcp.core.ui.inputchat.BCPInputChat
                            android:id="@+id/bcpInputChat"
                            android:layout_height="wrap_content"
                            android:layout_width="match_parent"
                            app:bcp_input_chat_hint="Utiliza el micrófono o teclado"
                            app:bcp_input_chat_mic_action_label="Micrófono"
                            app:bcp_input_chat_mic_content_description="Hablar"
                            app:bcp_input_chat_send_action_label="Enviar"
                            app:bcp_input_chat_send_content_description="Enviar mensaje"
                            app:bcp_input_chat_style="mic" />
                        ```
                        
                        #### Kotlin
                        
                        ```kotlin
                        // El campo de texto será anunciado como: "Utiliza el micrófono o teclado, campo de texto,toca dos veces para editar texto." -->
                        // El campo del botón en modo microfono será anunciado como: "Micrófono,botón,toca dos veces para hablar." -->
                        // El campo del botón en modo envío será anunciado como: "Enviar,botón,toca dos veces para enviar mensaje." -->
                        bcpInputChat.hint = "Utiliza el micrófono o teclado"
                        bcpInputChat.micActionLabel = "Micrófono"
                        bcpInputChat.micContentDescription = "Hablar"
                        bcpInputChat.sendActionLabel = "Enviar"
                        bcpInputChat.sendContentDescription = "Enviar mensaje"
                        ```
                        
                        #### Demo
                        ---
                        [<img src="../../../../../../../../../images/components/bcpinputchat/bcpinputchat.gif" width="300"/>]()
                        ---
                        
                        ## 🖼️ Showcase
                        
                        Puedes ver este componente en acción en nuestro **Showcase** oficial:
                        
                        🔗 [Repositorio UI-Components - Showcase](https://github.com/BCP-Framework-Facilidades/nfmc-bcp-android-core-ui-components-showcase/blob/develop/ui-component-showcase/src/main/java/com/bcp/core/ui/sample/component/input/BCPInputChatActivity.kt)
                        
                        ---
                          
                      
                    """.trimIndent()


                    Markdown(
                        markdown = markdownSample,
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}
