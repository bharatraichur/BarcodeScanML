package com.codebhar.barcodescanml

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import com.codebhar.barcodescanml.ui.theme.BarcodeScanMLTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalGetImage
class MainActivity : ComponentActivity() {

    private fun navigateToBarcodeScan() {
        val barcodeScanIntent = Intent(this, BarcodeScanActivity::class.java)
        startActivity(barcodeScanIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarcodeScanMLTheme {
                MainScreen(
                    onButtonClick = ::navigateToBarcodeScan
                )
            }
        }
    }
}