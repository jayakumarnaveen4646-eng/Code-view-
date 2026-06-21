package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiTutorService {
    private const val TAG = "GeminiTutorService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getTutorResponse(
        prompt: String,
        systemInstruction: String = "You are Codey, an encouraging and expert coding tutor for Python, Java, JavaScript, and HTML. Respond with clear, direct instruction. Keep it clean and concise, and format code elegantly.",
        history: List<Pair<String, Boolean>> = emptyList() // Pair<Message, IsUser>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Tutor Offline: Gemini API Key is missing or invalid. Please configure your API key in the AI Studio Secrets panel."
        }

        try {
            val rootObj = JSONObject()

            // System instructions
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            rootObj.put("systemInstruction", sysInstructionObj)

            // Dynamic contents configuration
            val contentsArray = JSONArray()

            // Map chat history if any
            for (chatTurn in history) {
                val contentObj = JSONObject()
                contentObj.put("role", if (chatTurn.second) "user" else "model")
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", chatTurn.first)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            // Current prompt turn
            val currentTurnObj = JSONObject()
            currentTurnObj.put("role", "user")
            val currentParts = JSONArray()
            val currentPart = JSONObject()
            currentPart.put("text", prompt)
            currentParts.put(currentPart)
            currentTurnObj.put("parts", currentParts)
            contentsArray.put(currentTurnObj)

            rootObj.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            rootObj.put("generationConfig", generationConfig)

            val requestBodyString = rootObj.toString()
            Log.d(TAG, "Request payload: $requestBodyString")

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyString.toRequestBody(mediaType))
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Response code: ${response.code}, payload: $responseBodyStr")

                if (!response.isSuccessful) {
                    return@withContext "Error context: (${response.code}) - Failed to connect to Tutor."
                }

                try {
                    val responseJson = JSONObject(responseBodyStr)
                    val candidates = responseJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        if (parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).getString("text")
                        }
                    }
                    return@withContext "Tutor responded with an empty answer."
                } catch (e: Exception) {
                    Log.e(TAG, "Failed parsing answer JSON", e)
                    return@withContext "Error parsing AI Tutor response."
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Tutor network request timeout", e)
            return@withContext "Connection to Codey timed out. Check your internet connection."
        } catch (e: Exception) {
            Log.e(TAG, "Tutor fetch Exception", e)
            return@withContext "An unexpected connection error occurred: ${e.localizedMessage}"
        }
    }
}
