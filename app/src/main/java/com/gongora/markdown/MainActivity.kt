package com.gongora.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gongora.markdown.showcase.MarkdownInputScreen
import com.gongora.markdown.showcase.MarkdownPreviewScreen
import com.gongora.markdown.ui.theme.MarkdownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "input",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("input") {
                            MarkdownInputScreen { markdown ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("markdown", markdown)
                                navController.navigate("preview")
                            }
                        }
                        composable("preview") {
                            val markdown = navController.previousBackStackEntry?.savedStateHandle?.get<String>("markdown") ?: ""
                            MarkdownPreviewScreen(markdown) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
