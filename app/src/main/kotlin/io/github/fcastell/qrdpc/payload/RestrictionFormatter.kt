package io.github.fcastell.qrdpc.payload

/** Renders a restriction's value for display; nested bundle/bundle_array as compact JSON. */
fun RestrictionValue.displayString(): String =
    when (this) {
        is RestrictionValue.BoolValue -> value.toString()
        is RestrictionValue.StringValue -> value
        is RestrictionValue.IntValue -> value.toString()
        is RestrictionValue.StringArrayValue -> value.joinToString(prefix = "[", postfix = "]")
        is RestrictionValue.BundleValue -> value.toCompactJson()
        is RestrictionValue.BundleArrayValue -> value.joinToString(prefix = "[", postfix = "]") { it.toCompactJson() }
    }

private fun Map<String, NestedValue>.toCompactJson(): String =
    entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\": ${value.toCompactJson()}" }

private fun NestedValue.toCompactJson(): String =
    when (this) {
        is NestedValue.NestedBool -> value.toString()
        is NestedValue.NestedString -> "\"$value\""
        is NestedValue.NestedInt -> value.toString()
        is NestedValue.NestedStringArray -> value.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        is NestedValue.NestedBundle -> value.toCompactJson()
        is NestedValue.NestedBundleArray -> value.joinToString(prefix = "[", postfix = "]") { it.toCompactJson() }
    }
