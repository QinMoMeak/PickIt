package com.pickit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pickit.app.presentation.navigation.PickItAppRoot
import com.pickit.app.presentation.theme.PickItTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickItTheme {
                PickItAppRoot()
            }
        }
    }
}
