package com.mohdroid.minesweeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.minesweeper.navigation.MsNavGraph
import com.mohdroid.minesweeper.navigation.ProvideNavigation

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MsTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ProvideNavigation {
                        MsNavGraph()
                    }
                }
            }
        }
    }
}
