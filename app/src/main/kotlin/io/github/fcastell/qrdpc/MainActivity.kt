package io.github.fcastell.qrdpc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.fcastell.qrdpc.scan.ScannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // targetSdk 36 draws edge-to-edge by default; without this, content
                    // (notably the Apply/Rescan buttons) renders underneath the system
                    // status/navigation bars and touches there are intercepted by the
                    // system UI instead of reaching the app.
                    ScannerScreen(modifier = Modifier.safeDrawingPadding())
                }
            }
        }
    }
}
