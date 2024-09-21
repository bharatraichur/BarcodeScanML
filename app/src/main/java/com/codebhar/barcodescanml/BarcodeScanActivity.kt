package com.codebhar.barcodescanml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codebhar.barcodescanml.ui.theme.BarcodeScanMLTheme
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeScanActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarcodeScanMLTheme {
                BarcodeScanView(
                    navigateUp = { this.finish() }
                )
            }
        }
    }
}