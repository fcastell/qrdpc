package io.github.fcastell.qrdpc.payload

/** A validated QrDPC QR payload, per the `qr-payload-format` contract. */
data class QrPayload(
    val schemaVersion: Int,
    val packageName: String,
    val restrictions: List<Restriction>,
)

data class Restriction(
    val key: String,
    val type: RestrictionType,
    val value: RestrictionValue,
)

enum class RestrictionType(
    val jsonName: String,
) {
    BOOL("bool"),
    STRING("string"),
    INTEGER("integer"),
    CHOICE("choice"),
    MULTI_SELECT("multi-select"),
    BUNDLE("bundle"),
    BUNDLE_ARRAY("bundle_array"),
    ;

    companion object {
        fun fromJsonName(name: String): RestrictionType? = entries.find { it.jsonName == name }
    }
}

/** A restriction's value, typed to match its declared [RestrictionType]. */
sealed interface RestrictionValue {
    data class BoolValue(
        val value: Boolean,
    ) : RestrictionValue

    data class StringValue(
        val value: String,
    ) : RestrictionValue

    data class IntValue(
        val value: Int,
    ) : RestrictionValue

    data class StringArrayValue(
        val value: List<String>,
    ) : RestrictionValue

    data class BundleValue(
        val value: Map<String, NestedValue>,
    ) : RestrictionValue

    data class BundleArrayValue(
        val value: List<Map<String, NestedValue>>,
    ) : RestrictionValue
}

/**
 * A value nested inside a `bundle`/`bundle_array` restriction, typed by inference from
 * its native JSON shape rather than an explicit `type` tag.
 */
sealed interface NestedValue {
    data class NestedBool(
        val value: Boolean,
    ) : NestedValue

    data class NestedString(
        val value: String,
    ) : NestedValue

    data class NestedInt(
        val value: Int,
    ) : NestedValue

    data class NestedStringArray(
        val value: List<String>,
    ) : NestedValue

    data class NestedBundle(
        val value: Map<String, NestedValue>,
    ) : NestedValue

    data class NestedBundleArray(
        val value: List<Map<String, NestedValue>>,
    ) : NestedValue
}

/** Result of parsing and validating a QR code's decoded string against `qr-payload-format`. */
sealed interface QrPayloadResult {
    data class Valid(
        val payload: QrPayload,
    ) : QrPayloadResult

    data class Invalid(
        val reason: String,
    ) : QrPayloadResult
}
