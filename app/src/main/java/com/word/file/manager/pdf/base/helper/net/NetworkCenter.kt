package com.word.file.manager.pdf.base.helper.net

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

typealias JsonString = String

object NetworkCenter {

    private const val MAX_ENQUEUE_COUNT = 5
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()

    fun session() {
        val obj = BaseInfo.buildObj().apply {
            put("conn", JSONObject())
        }
        obj.toString().enqueueRequest()
    }

    fun adImpression(adObj: JSONObject) {
        val obj = BaseInfo.buildObj().apply {
            put("silvery", adObj)
        }
        obj.toString().enqueueRequest()
    }

    fun installEvent(block: (JSONObject) -> Unit) {
        val obj = BaseInfo.buildObj().apply {
            put("polio", "welfare")
        }
        block.invoke(obj)
        obj.toString().enqueueRequest()
    }

    fun customEvent(eventName: String, params: Map<String, Any?> = mapOf()) {
        val obj = BaseInfo.buildObj().apply {
            put("polio", eventName)
            params.onEach { e -> put("${e.key}|clue", e.value) }
        }
        obj.toString().enqueueRequest()
    }

    private fun JsonString.enqueueRequest(
        url: String = BaseInfo.baseUrl,
        maxCount: Int = MAX_ENQUEUE_COUNT,
        onSuccess: (String) -> Unit = {},
        onFailure: (IOException) -> Unit = {},
    ) {
        val retryLimit = maxCount.coerceIn(1, MAX_ENQUEUE_COUNT)
        enqueueInternal(
            request = buildJsonPostRequest(url),
            currentCount = 1,
            maxCount = retryLimit,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    private fun JsonString.buildJsonPostRequest(url: String): Request {
        return Request.Builder().url(url).post(toRequestBody(jsonMediaType)).build()
    }

    private fun enqueueInternal(
        request: Request,
        currentCount: Int,
        maxCount: Int,
        onSuccess: (String) -> Unit,
        onFailure: (IOException) -> Unit,
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                retryOrFail(request, currentCount, maxCount, e, onSuccess, onFailure)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        onSuccess(it.body.string())
                    } else {
                        retryOrFail(
                            request = request,
                            currentCount = currentCount,
                            maxCount = maxCount,
                            exception = IOException("Http ${it.code}"),
                            onSuccess = onSuccess,
                            onFailure = onFailure,
                        )
                    }
                }
            }
        })
    }

    private fun retryOrFail(
        request: Request,
        currentCount: Int,
        maxCount: Int,
        exception: IOException,
        onSuccess: (String) -> Unit,
        onFailure: (IOException) -> Unit,
    ) {
        if (currentCount >= maxCount) {
            onFailure(exception)
            return
        }
        enqueueInternal(
            request = request,
            currentCount = currentCount + 1,
            maxCount = maxCount,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

}
