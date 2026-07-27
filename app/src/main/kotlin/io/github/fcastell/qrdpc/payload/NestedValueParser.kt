package io.github.fcastell.qrdpc.payload

import org.json.JSONArray
import org.json.JSONObject

/** Accepts JSON numbers that are integral and fit in an [Int]; rejects everything else. */
internal fun asInt(raw: Any?): Int? =
    when (raw) {
        is Int -> raw
        is Long -> raw.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
        else -> null
    }

internal fun asStringArray(raw: Any?): List<String>? {
    val array = raw as? JSONArray ?: return null
    return (0 until array.length()).map { i -> array.opt(i) as? String ?: return null }
}

internal fun asNestedMap(obj: JSONObject): Map<String, NestedValue> {
    val result = mutableMapOf<String, NestedValue>()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        result[key] = parseNestedValue(obj.opt(key)) ?: invalid("nested value for \"$key\" is not a supported type.")
    }
    return result
}

internal fun asNestedMapList(array: JSONArray): List<Map<String, NestedValue>> =
    (0 until array.length()).map { i ->
        val item = array.opt(i) as? JSONObject ?: invalid("bundle_array element [$i] is not a JSON object.")
        asNestedMap(item)
    }

/**
 * Infers a nested value's type from its native JSON shape (object -> nested Bundle,
 * array-of-objects -> Bundle array, array-of-strings -> String array, boolean/string
 * pass through, integral numbers become Int). Non-integral numbers (no float/double
 * equivalent in RESTRICTION_TYPE) and explicit JSON null are rejected. An empty array is
 * treated as an empty string array, since an empty JSON array can't otherwise be
 * distinguished from an empty bundle array.
 */
private fun parseNestedValue(raw: Any?): NestedValue? =
    when (raw) {
        is Boolean -> NestedValue.NestedBool(raw)
        is String -> NestedValue.NestedString(raw)
        is Int -> NestedValue.NestedInt(raw)
        is Long -> asInt(raw)?.let { NestedValue.NestedInt(it) }
        is JSONObject -> NestedValue.NestedBundle(asNestedMap(raw))
        is JSONArray -> parseNestedArray(raw)
        else -> null
    }

private fun parseNestedArray(array: JSONArray): NestedValue? =
    when {
        array.length() == 0 -> NestedValue.NestedStringArray(emptyList())
        array.opt(0) is JSONObject -> NestedValue.NestedBundleArray(asNestedMapList(array))
        else -> asStringArray(array)?.let { NestedValue.NestedStringArray(it) }
    }
