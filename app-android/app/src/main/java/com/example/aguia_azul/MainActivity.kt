package com.example.aguia_azul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.aguia_azul.ui.navigation.AppNavigation
import com.example.aguia_azul.ui.theme.Aguia_azulTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Aguia_azulTheme {
                AppNavigation()
            }
        }
    }
}
