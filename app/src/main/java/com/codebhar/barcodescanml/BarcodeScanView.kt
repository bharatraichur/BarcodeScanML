package com.codebhar.barcodescanml

import android.annotation.SuppressLint
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlin.math.abs

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarcodeScanView(
    navigateUp: () -> Unit
) {
    val inspection = LocalInspectionMode.current

    val cameraPermission = when (inspection) {
        true -> rememberPreviewPermissionState(granted = false)
        false -> rememberPermissionState(android.Manifest.permission.CAMERA)
    }

    val camera = remember {
        ScannerCamera()
    }

    var flashlightIcon by remember {
        mutableStateOf(false)
    }

    var lastScannedBarcode by remember {
        mutableStateOf<String>("")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        var screenHeight by remember { mutableStateOf(0) }
        var screenWidth by remember { mutableStateOf(0) }
        var verticalOffset by remember { mutableStateOf(0) }

        val scanAreaWidth = screenWidth * 0.95f
        val scanAreaHeight = screenHeight * 0.225f

        val scanArea = Rect(
            offset = Offset(
                x = (screenWidth - scanAreaWidth) / 2f,
                y = (screenHeight - scanAreaHeight) / 2f - verticalOffset
            ),
            size = Size(
                width = scanAreaWidth,
                height = scanAreaHeight
            ),
        )

        if (!cameraPermission.status.isGranted) {
            if (cameraPermission.status.shouldShowRationale) {
                AlertDialogView(
                    navigateUp = navigateUp,
                    title = stringResource(id = R.string.camera_access_error_title),
                    description = stringResource(id = R.string.camera_access_error_description)
                )
            } else {
                cameraPermission.launchPermissionRequest()
            }
        } else {
            camera.CameraPreview(
                scanArea = scanArea,
                onBarcodeScanned = { barcode ->
                    barcode?.displayValue?.let {
                        lastScannedBarcode = it
                    }
                }
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            clipPath(
                path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = scanArea,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    )
                },
                clipOp = ClipOp.Difference
            ) {
                drawRect(Color(0x99000000))
            }
        }

        key(scanArea) {
            CameraOverlaySubComposeLayout(
                topContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            navigateUp.invoke()
                        }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back button"
                            )
                        }
                        IconButton(onClick = {
                            flashlightIcon = !flashlightIcon
                            camera.toggleFlashlight(flashlightIcon)
                        }) {
                            Image(
                                painter = painterResource(id = if (flashlightIcon) {
                                    R.drawable.ic_flashlight_off
                                } else {
                                    R.drawable.ic_flashlight_on
                                }),
                                contentDescription = "Turn the Flash on or off"
                            )
                        }
                    }
                },
                windowProxy = {
                    with(LocalDensity.current) {
                        Box(
                            modifier = Modifier
                                .width(scanAreaWidth.toDp())
                                .height(scanAreaHeight.toDp())
                        )
                    }
                },
                descriptionContent = {
                    Text(
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            letterSpacing = 0.1.sp
                        ),
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        text = stringResource(id = R.string.scan_your_barcode),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 32.dp,
                                vertical = 16.dp
                            )
                    )
                },
                bottomContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color.DarkGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )
                    ) {
                        Text(
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp
                            ),
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            text = stringResource(id = R.string.scan_your_barcode_description),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                onMeasuredVerticalOffset = { width, height, offset ->
                    screenWidth = width
                    screenHeight = height
                    verticalOffset = offset
                }
            )

            if (lastScannedBarcode.isNotEmpty()) {
                AlertDialogView(
                    navigateUp = navigateUp,
                    title = stringResource(id = R.string.scanned_barcode),
                    description = lastScannedBarcode.let {
                        stringResource(id = R.string.scanned_barcode_data, it)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraOverlaySubComposeLayout(
    topContent: @Composable () -> Unit,
    windowProxy: @Composable () -> Unit,
    descriptionContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    onMeasuredVerticalOffset: (Int, Int, Int) -> Unit
) = SubcomposeLayout { constraints ->
    layout(constraints.maxWidth, constraints.maxHeight) {
        val topPlaceable = subcompose("top", topContent).map {
            it.measure(constraints)
        }

        topPlaceable.first().placeRelative(0, 0)

        val bottomPlaceable = subcompose("bottom", bottomContent).map {
            it.measure(constraints)
        }.first()

        bottomPlaceable.let {
            it.placeRelative(0, constraints.maxHeight - it.height)
        }

        val mainPlaceable = subcompose("main", windowProxy).map {
            it.measure(constraints)
        }.first()

        val horizontalCentre = constraints.maxWidth / 2
        val verticalCentre = constraints.maxHeight / 2
        val mainX = horizontalCentre - mainPlaceable.width / 2
        val mainY = verticalCentre - mainPlaceable.height / 2
        val windowBottom = mainY + mainPlaceable.height

        val windowContentPlaceable = subcompose("window-label", descriptionContent).map {
            it.measure(constraints)
        }.first()

        var requiredOffset = 0
        val spaceForContent = constraints.maxHeight - windowBottom - bottomPlaceable.height
        if (windowContentPlaceable.height > spaceForContent) {
            requiredOffset = abs(windowContentPlaceable.height - spaceForContent)
        }

        val measuredMainY = mainY - requiredOffset

        mainPlaceable.placeRelative(mainX, measuredMainY)
        windowContentPlaceable.placeRelative(0, measuredMainY + mainPlaceable.height)
        onMeasuredVerticalOffset(constraints.maxWidth, constraints.maxHeight, requiredOffset)
    }
}

@Composable
fun AlertDialogView(
    navigateUp: () -> Unit,
    title: String,
    description: String
) {
    AlertDialog(
        onDismissRequest = navigateUp,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            Button(onClick = navigateUp) {
                Text(text = "OK")
            }
        }
    )
}

@Preview
@Composable
fun AlertDialogViewPreview() {
    AlertDialogView(
        navigateUp = {},
        title = stringResource(id = R.string.camera_access_error_title),
        description = stringResource(id = R.string.camera_access_error_description)
    )
}

@OptIn(ExperimentalPermissionsApi::class)
internal class PreviewPermissionState(
    override val status: PermissionStatus
) : PermissionState {
    override val permission: String
        get() = ""

    override fun launchPermissionRequest() = Unit
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun rememberPreviewPermissionState(granted: Boolean): PermissionState {
    val status = when (granted) {
        true -> PermissionStatus.Granted
        false -> PermissionStatus.Denied(shouldShowRationale = false)
    }

    return remember(granted) { PreviewPermissionState(status = status) }
}

@Preview(showSystemUi = true)
@Composable
fun BarcodeScanPreview() {
    BarcodeScanView(
        navigateUp = { }
    )
}