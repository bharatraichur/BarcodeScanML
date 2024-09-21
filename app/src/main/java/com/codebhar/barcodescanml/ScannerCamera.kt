package com.codebhar.barcodescanml

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class ScannerCamera {

    private lateinit var cameraController: CameraController

    @Composable
    fun CameraPreview(
        scanArea: Rect,
        onBarcodeScanned: (Barcode?) -> Unit
    ) {
        val localContext = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        key(scanArea) {
            cameraController = LifecycleCameraController(localContext).apply {
                bindToLifecycle(lifecycleOwner)
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            }

            val previewView = remember {
                PreviewView(localContext).apply {
                    controller = cameraController
                }
            }

            val executor = ContextCompat.getMainExecutor(localContext)

            LaunchedEffect(previewView) {
                val options = BarcodeScannerOptions.Builder().build()

                val scanner = BarcodeScanning.getClient(options)

                val imageAnalyzer = MlKitAnalyzer(
                    listOf(scanner),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    executor
                ) { result: MlKitAnalyzer.Result? ->
                    val barcode = result?.getValue(scanner)?.firstOrNull()
                    if (barcode != null && !barcode.rawValue.isNullOrEmpty()) {
                        barcode.boundingBox?.let {
                            if (scanArea.toAndroidRectF().contains(it.toRectF())) {
                                onBarcodeScanned(barcode)
                                scanner.close()
                            }
                        }
                    }
                }

                cameraController.apply {
                    setImageAnalysisAnalyzer(
                        executor,
                        imageAnalyzer
                    )
                    setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    previewView
                }
            )
        }
    }

    fun toggleFlashlight(shouldTurnOn: Boolean) {
        if (cameraController.cameraInfo?.hasFlashUnit() == true) {
            cameraController.cameraControl?.enableTorch(shouldTurnOn)
        }
    }
}