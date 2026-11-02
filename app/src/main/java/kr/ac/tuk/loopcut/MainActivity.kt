package kr.ac.tuk.loopcut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.toArgb

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.light(Bg.toArgb(),Bg.toArgb()),navigationBarStyle=SystemBarStyle.light(Bg.toArgb(),Bg.toArgb()))
        setContent { LoopCutTheme { LoopCutApp() } }
    }
}
