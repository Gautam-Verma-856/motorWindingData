package com.example

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MotorRepositoryTest {

    @Test
    fun testImgBBSuccessParsing() {
        val jsonResponse = """
            {
                "data": {
                    "id": "2ndCYJK",
                    "title": "sample",
                    "url_viewer": "https://ibb.co/2ndCYJK",
                    "url": "https://i.ibb.co/w04Prt6/sample.jpg",
                    "display_url": "https://i.ibb.co/w04Prt6/sample.jpg"
                },
                "success": true,
                "status": 200
            }
        """.trimIndent()

        val jsonObject = JSONObject(jsonResponse)
        val isSuccess = jsonObject.optBoolean("success", false) || jsonObject.optInt("status", 0) == 200
        assertTrue(isSuccess)

        val data = jsonObject.optJSONObject("data")
        val directUrl = data?.optString("url")?.takeIf { it.isNotEmpty() }
            ?: data?.optString("display_url")?.takeIf { it.isNotEmpty() }

        assertEquals("https://i.ibb.co/w04Prt6/sample.jpg", directUrl)
    }

    @Test
    fun testImgBB400ErrorParsing() {
        val raw400Response = """
            {"status_code":400,"error":{"message":"Invalid API v1 key.","code":100},"status_txt":"Bad Request"}
        """.trimIndent()

        val json = JSONObject(raw400Response)
        val errorObj = json.optJSONObject("error")
        val message = errorObj?.optString("message")?.trim() ?: ""

        val userFriendlyMessage = when {
            message.contains("Invalid API v1 key", ignoreCase = true) ||
            message.contains("Empty API v1 key", ignoreCase = true) -> {
                "Invalid ImgBB API key. Please check your key in AI Studio Secrets."
            }
            message.contains("forbidden", ignoreCase = true) -> {
                "ImgBB API key is forbidden or revoked. Please provide a new key in AI Studio Secrets."
            }
            message.isNotEmpty() -> message
            else -> "Upload failed"
        }

        assertEquals("Invalid ImgBB API key. Please check your key in AI Studio Secrets.", userFriendlyMessage)
        assertFalse(userFriendlyMessage.contains("status_code"))
        assertFalse(userFriendlyMessage.contains("100"))
    }

    @Test
    fun testImgBBForbiddenErrorParsing() {
        val rawForbiddenResponse = """
            {"status_code":400,"error":{"message":"You have been forbidden to use this website.","code":103},"status_txt":"Bad Request"}
        """.trimIndent()

        val json = JSONObject(rawForbiddenResponse)
        val errorObj = json.optJSONObject("error")
        val message = errorObj?.optString("message")?.trim() ?: ""

        val userFriendlyMessage = when {
            message.contains("Invalid API v1 key", ignoreCase = true) ||
            message.contains("Empty API v1 key", ignoreCase = true) -> {
                "Invalid ImgBB API key. Please check your key in AI Studio Secrets."
            }
            message.contains("forbidden", ignoreCase = true) -> {
                "ImgBB API key is forbidden or revoked. Please provide a new key in AI Studio Secrets."
            }
            message.isNotEmpty() -> message
            else -> "Upload failed"
        }

        assertEquals("ImgBB API key is forbidden or revoked. Please provide a new key in AI Studio Secrets.", userFriendlyMessage)
    }
}
