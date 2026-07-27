package io.github.fcastell.qrdpc.payload

import android.os.Bundle

/** Converts a validated [QrPayload]'s restrictions into an `android.os.Bundle`. */
fun QrPayload.toApplicationRestrictionsBundle(): Bundle {
    val bundle = Bundle()
    for (restriction in restrictions) {
        putRestriction(bundle, restriction)
    }
    return bundle
}

private fun putRestriction(
    bundle: Bundle,
    restriction: Restriction,
) {
    val key = restriction.key
    when (val value = restriction.value) {
        is RestrictionValue.BoolValue -> {
            bundle.putBoolean(key, value.value)
        }

        is RestrictionValue.StringValue -> {
            bundle.putString(key, value.value)
        }

        is RestrictionValue.IntValue -> {
            bundle.putInt(key, value.value)
        }

        is RestrictionValue.StringArrayValue -> {
            bundle.putStringArray(key, value.value.toTypedArray())
        }

        is RestrictionValue.BundleValue -> {
            bundle.putBundle(key, value.value.toBundle())
        }

        is RestrictionValue.BundleArrayValue -> {
            bundle.putParcelableArray(key, value.value.map { it.toBundle() }.toTypedArray())
        }
    }
}

private fun Map<String, NestedValue>.toBundle(): Bundle {
    val bundle = Bundle()
    for ((key, value) in this) {
        putNestedValue(bundle, key, value)
    }
    return bundle
}

private fun putNestedValue(
    bundle: Bundle,
    key: String,
    value: NestedValue,
) {
    when (value) {
        is NestedValue.NestedBool -> {
            bundle.putBoolean(key, value.value)
        }

        is NestedValue.NestedString -> {
            bundle.putString(key, value.value)
        }

        is NestedValue.NestedInt -> {
            bundle.putInt(key, value.value)
        }

        is NestedValue.NestedStringArray -> {
            bundle.putStringArray(key, value.value.toTypedArray())
        }

        is NestedValue.NestedBundle -> {
            bundle.putBundle(key, value.value.toBundle())
        }

        is NestedValue.NestedBundleArray -> {
            bundle.putParcelableArray(key, value.value.map { it.toBundle() }.toTypedArray())
        }
    }
}
