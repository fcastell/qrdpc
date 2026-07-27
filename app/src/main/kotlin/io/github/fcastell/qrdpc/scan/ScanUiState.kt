package io.github.fcastell.qrdpc.scan

import io.github.fcastell.qrdpc.payload.QrPayload

/** UI state machine for the scan → confirm → apply flow. */
sealed interface ScanUiState {
    /** Camera preview active, analyzing frames. */
    data object Scanning : ScanUiState

    /** A QR code was detected but its content didn't validate against `qr-payload-format`. */
    data class DecodeError(
        val message: String,
    ) : ScanUiState

    /** A valid payload is awaiting explicit user confirmation before anything is applied. */
    data class Confirming(
        val payload: QrPayload,
    ) : ScanUiState

    /** The user tapped Apply; the restrictions call is in flight. */
    data object Applying : ScanUiState

    /** Restrictions were applied successfully. */
    data object Applied : ScanUiState

    /** Applying the restrictions failed; [message] is specific to the failure cause. */
    data class ApplyFailed(
        val message: String,
    ) : ScanUiState
}
