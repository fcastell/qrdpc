package io.github.fcastell.qrdpc.restrictions

import io.github.fcastell.qrdpc.payload.QrPayload

/** Outcome of attempting to apply a [QrPayload]'s restrictions to its target package. */
sealed interface ApplyResult {
    data object Success : ApplyResult

    data object PackageNotInstalled : ApplyResult

    data object DelegationNotGranted : ApplyResult

    data class OtherFailure(
        val message: String,
    ) : ApplyResult
}
