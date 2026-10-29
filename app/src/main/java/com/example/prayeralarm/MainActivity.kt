package com.example.prayeralarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.prayeralarm.ui.navigation.MainScreen
import com.example.prayeralarm.ui.theme.PrayerAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayerAlarmTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }
}
