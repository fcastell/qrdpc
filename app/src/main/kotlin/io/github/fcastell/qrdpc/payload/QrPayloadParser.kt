package io.github.fcastell.qrdpc.payload

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val SUPPORTED_SCHEMA_VERSION = 1

/** Internal control-flow signal for a validation failure; never escapes [parseQrPayload]. */
internal class InvalidPayloadException(
    val reason: String,
) : Exception(reason)

internal fun invalid(reason: String): Nothing = throw InvalidPayloadException(reason)

/**
 * Parses [json] (already BOM-stripped) into a [QrPayload], validating every field against
 * the `qr-payload-format` contract. Never throws — validation failures are returned as
 * [QrPayloadResult.Invalid] with a specific reason.
 */
fun parseQrPayload(json: String): QrPayloadResult =
    try {
        QrPayloadResult.Valid(parseQrPayloadOrThrow(json))
    } catch (e: InvalidPayloadException) {
        QrPayloadResult.Invalid(e.reason)
    }

private fun parseQrPayloadOrThrow(json: String): QrPayload {
    val root =
        try {
            JSONObject(json)
        } catch (e: JSONException) {
            invalid("Could not parse QR content as JSON: ${e.message}")
        }

    val schemaVersion = asInt(root.opt("schemaVersion")) ?: invalid("Missing or invalid \"schemaVersion\".")
    if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
        invalid("Unsupported schemaVersion $schemaVersion (this app supports $SUPPORTED_SCHEMA_VERSION).")
    }

    val packageName = root.opt("packageName") as? String
    if (packageName.isNullOrBlank()) invalid("Missing or invalid \"packageName\".")

    val restrictionsRaw =
        root.opt("restrictions") as? JSONArray
            ?: invalid("Missing or invalid \"restrictions\" array.")
    val restrictions =
        (0 until restrictionsRaw.length()).map { i ->
            val entry = restrictionsRaw.opt(i) as? JSONObject ?: invalid("restrictions[$i] is not a JSON object.")
            parseRestriction(entry, i)
        }

    return QrPayload(schemaVersion, packageName, restrictions)
}

private fun parseRestriction(
    entry: JSONObject,
    index: Int,
): Restriction {
    val key = entry.opt("key") as? String
    if (key.isNullOrBlank()) invalid("restrictions[$index] has a missing or empty \"key\".")

    val typeName = entry.opt("type")
    val type =
        (typeName as? String)?.let { RestrictionType.fromJsonName(it) }
            ?: invalid("restrictions[$index] (\"$key\") has an unknown \"type\": $typeName.")

    val value =
        parseRestrictionValue(type, entry.opt("value"))
            ?: invalid("restrictions[$index] (\"$key\"): value does not match declared type \"${type.jsonName}\".")

    return Restriction(key, type, value)
}

private fun parseRestrictionValue(
    type: RestrictionType,
    raw: Any?,
): RestrictionValue? =
    when (type) {
        RestrictionType.BOOL -> {
            (raw as? Boolean)?.let { RestrictionValue.BoolValue(it) }
        }

        RestrictionType.STRING, RestrictionType.CHOICE -> {
            (raw as? String)?.let { RestrictionValue.StringValue(it) }
        }

        RestrictionType.INTEGER -> {
            asInt(raw)?.let { RestrictionValue.IntValue(it) }
        }

        RestrictionType.MULTI_SELECT -> {
            asStringArray(raw)?.let { RestrictionValue.StringArrayValue(it) }
        }

        RestrictionType.BUNDLE -> {
            parseBundleValue(raw)
        }

        RestrictionType.BUNDLE_ARRAY -> {
            parseBundleArrayValue(raw)
        }
    }

private fun parseBundleValue(raw: Any?): RestrictionValue.BundleValue? {
    val obj = raw as? JSONObject ?: return null
    return RestrictionValue.BundleValue(asNestedMap(obj))
}

private fun parseBundleArrayValue(raw: Any?): RestrictionValue.BundleArrayValue? {
    val array = raw as? JSONArray ?: return null
    return RestrictionValue.BundleArrayValue(asNestedMapList(array))
}
