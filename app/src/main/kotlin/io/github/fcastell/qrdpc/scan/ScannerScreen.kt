package io.github.fcastell.qrdpc.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.fcastell.qrdpc.payload.QrPayloadResult
import io.github.fcastell.qrdpc.payload.parseQrPayload
import io.github.fcastell.qrdpc.restrictions.ApplyResult
import io.github.fcastell.qrdpc.restrictions.applyRestrictions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by
        remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED,
            )
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted -> hasCameraPermission = granted }

    if (hasCameraPermission) {
        ScanFlow(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant camera permission")
            }
        }
    }
}

@Composable
private fun ScanFlow(modifier: Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<ScanUiState>(ScanUiState.Scanning) }
    val analyzer =
        remember {
            QrCodeAnalyzer { text ->
                uiState =
                    when (val result = parseQrPayload(text)) {
                        is QrPayloadResult.Valid -> ScanUiState.Confirming(result.payload)
                        is QrPayloadResult.Invalid -> ScanUiState.DecodeError(result.reason)
                    }
            }
        }

    fun rescan() {
        analyzer.resume()
        uiState = ScanUiState.Scanning
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ScanUiState.Scanning -> {
                CameraPreview(analyzer, Modifier.fillMaxSize())
            }

            is ScanUiState.DecodeError -> {
                ErrorScreen(message = state.message, onRescan = ::rescan)
            }

            is ScanUiState.Confirming -> {
                ConfirmationScreen(
                    payload = state.payload,
                    onRescan = ::rescan,
                    onApply = {
                        uiState = ScanUiState.Applying
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = applyRestrictions(context, state.payload)
                            withContext(Dispatchers.Main) {
                                uiState = result.toUiState(state.payload.packageName)
                            }
                        }
                    },
                )
            }

            is ScanUiState.Applying -> {
                LoadingScreen()
            }

            is ScanUiState.Applied -> {
                SuccessScreen(onScanAnother = ::rescan)
            }

            is ScanUiState.ApplyFailed -> {
                ErrorScreen(message = state.message, onRescan = ::rescan)
            }
        }
    }
}

private fun ApplyResult.toUiState(packageName: String): ScanUiState =
    when (this) {
        ApplyResult.Success -> {
            ScanUiState.Applied
        }

        ApplyResult.PackageNotInstalled -> {
            ScanUiState.ApplyFailed("Target app \"$packageName\" is not installed.")
        }

        ApplyResult.DelegationNotGranted -> {
            ScanUiState.ApplyFailed(
                "QrDPC hasn't been delegated the APP_RESTRICTIONS scope for \"$packageName\" " +
                    "by the active device policy controller.",
            )
        }

        is ApplyResult.OtherFailure -> {
            ScanUiState.ApplyFailed(message)
        }
    }
