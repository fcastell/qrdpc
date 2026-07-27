package io.github.fcastell.qrdpc.payload

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class QrPayloadParserTest {
    @Test
    fun `valid payload with every restriction type parses correctly`() {
        val json =
            """
            {
              "schemaVersion": 1,
              "packageName": "com.example.target",
              "restrictions": [
                { "key": "serverUrl", "type": "string", "value": "https://example.com" },
                { "key": "isEnabled", "type": "bool", "value": true },
                { "key": "maxRetries", "type": "integer", "value": 3 },
                { "key": "mode", "type": "choice", "value": "production" },
                { "key": "allowedDomains", "type": "multi-select", "value": ["a.com", "b.com"] },
                { "key": "advanced", "type": "bundle", "value": {"timeout": 30, "debug": false} },
                { "key": "servers", "type": "bundle_array", "value": [{"host": "a"}, {"host": "b"}] }
              ]
            }
            """.trimIndent()

        val payload = assertValid(parseQrPayload(json))

        assertEquals(1, payload.schemaVersion)
        assertEquals("com.example.target", payload.packageName)
        assertEquals(7, payload.restrictions.size)

        assertEquals(
            RestrictionValue.StringValue("https://example.com"),
            payload.restrictions[0].value,
        )
        assertEquals(RestrictionValue.BoolValue(true), payload.restrictions[1].value)
        assertEquals(RestrictionValue.IntValue(3), payload.restrictions[2].value)
        assertEquals(RestrictionValue.StringValue("production"), payload.restrictions[3].value)
        assertEquals(
            RestrictionValue.StringArrayValue(listOf("a.com", "b.com")),
            payload.restrictions[4].value,
        )
        assertEquals(
            RestrictionValue.BundleValue(
                mapOf(
                    "timeout" to NestedValue.NestedInt(30),
                    "debug" to NestedValue.NestedBool(false),
                ),
            ),
            payload.restrictions[5].value,
        )
        assertEquals(
            RestrictionValue.BundleArrayValue(
                listOf(
                    mapOf("host" to NestedValue.NestedString("a")),
                    mapOf("host" to NestedValue.NestedString("b")),
                ),
            ),
            payload.restrictions[6].value,
        )
    }

    @Test
    fun `malformed JSON is invalid`() {
        assertInvalid(parseQrPayload("not json"))
    }

    @Test
    fun `missing schemaVersion is invalid`() {
        val json = """{"packageName": "com.example.target", "restrictions": []}"""
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `unsupported schemaVersion is invalid`() {
        val json = """{"schemaVersion": 2, "packageName": "com.example.target", "restrictions": []}"""
        val result = assertInvalid(parseQrPayload(json))
        assertTrue(result.reason.contains("2"))
    }

    @Test
    fun `missing packageName is invalid`() {
        val json = """{"schemaVersion": 1, "restrictions": []}"""
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `blank packageName is invalid`() {
        val json = """{"schemaVersion": 1, "packageName": "  ", "restrictions": []}"""
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `missing restrictions array is invalid`() {
        val json = """{"schemaVersion": 1, "packageName": "com.example.target"}"""
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `restriction missing key is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"type": "string", "value": "x"}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `restriction with unknown type is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "float", "value": 1.5}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `bool type with string value is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "bool", "value": "true"}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `integer type with non-integral value is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "integer", "value": 1.5}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `multi-select with a non-string element is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "multi-select", "value": ["a", 1]}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `bundle with a non-integral nested number is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "bundle", "value": {"ratio": 1.5}}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `bundle_array with a non-object element is invalid`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "bundle_array", "value": ["not-an-object"]}]}
            """.trimIndent()
        assertInvalid(parseQrPayload(json))
    }

    @Test
    fun `nested array of strings inside a bundle is inferred as a string array`() {
        val json =
            """
            {"schemaVersion": 1, "packageName": "com.example.target",
             "restrictions": [{"key": "k", "type": "bundle", "value": {"tags": ["a", "b"]}}]}
            """.trimIndent()
        val payload = assertValid(parseQrPayload(json))
        assertEquals(
            RestrictionValue.BundleValue(mapOf("tags" to NestedValue.NestedStringArray(listOf("a", "b")))),
            payload.restrictions[0].value,
        )
    }

    private fun assertValid(result: QrPayloadResult): QrPayload =
        (result as? QrPayloadResult.Valid)?.payload ?: fail("expected Valid, got $result") as Nothing

    private fun assertInvalid(result: QrPayloadResult): QrPayloadResult.Invalid =
        result as? QrPayloadResult.Invalid ?: fail("expected Invalid, got $result") as Nothing
}
