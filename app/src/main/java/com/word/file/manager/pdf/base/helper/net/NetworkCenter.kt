package com.word.file.manager.pdf.base.helper.net

import android.os.Build
import com.word.file.manager.pdf.BuildConfig
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.net.BaseInfo.deviceId
import com.word.file.manager.pdf.base.utils.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private const val RETRY_DELAY_MS = 100_000L
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()
    private val retryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    fun cloak() {
        if (LocalPrefs.hasReqCloak) return
        val obj = JSONObject().apply {
            put("mist", "com.agile.pdf.view")
            put("penguin", "vacate")
            put("nitride", BuildConfig.VERSION_NAME)
            put("cold", deviceId)
            put("nebular", System.currentTimeMillis())
            put("whipple", Build.MODEL ?: "")
            put("shako", Build.VERSION.RELEASE ?: "")
            put("stub", Build.BRAND ?: "")
        }
        obj.toString().enqueueRequest(url = BaseInfo.cloakUrl, maxCount = 10, retryDelayMs = 10_000L, onSuccess = {
            LocalPrefs.hasReqCloak = true
            LocalPrefs.userIsBlack = "inca" == it
        })
    }

    private fun JsonString.enqueueRequest(
        url: String = BaseInfo.baseUrl,
        maxCount: Int = MAX_ENQUEUE_COUNT,
        retryDelayMs: Long = RETRY_DELAY_MS,
        onSuccess: (String) -> Unit = {},
        onFailure: (IOException) -> Unit = {},
    ) {
        val retryLimit = maxCount.coerceIn(1, MAX_ENQUEUE_COUNT)
        enqueueInternal(
            request = buildJsonPostRequest(url),
            currentCount = 1,
            maxCount = retryLimit,
            retryDelayMs = retryDelayMs.coerceAtLeast(0L),
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
        retryDelayMs: Long,
        onSuccess: (String) -> Unit,
        onFailure: (IOException) -> Unit,
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                retryOrFail(request, currentCount, maxCount, retryDelayMs, e, onSuccess, onFailure)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseText = it.body.string()
                        "Network request success: $responseText".showLog()
                        onSuccess(responseText)
                    } else {
                        retryOrFail(
                            request = request,
                            currentCount = currentCount,
                            maxCount = maxCount,
                            retryDelayMs = retryDelayMs,
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
        retryDelayMs: Long,
        exception: IOException,
        onSuccess: (String) -> Unit,
        onFailure: (IOException) -> Unit,
    ) {
        "Network request failed($currentCount/$maxCount): ${exception.message.orEmpty()}".showLog()
        if (currentCount >= maxCount) {
            onFailure(exception)
            return
        }
        retryScope.launch {
            delay(retryDelayMs)
            enqueueInternal(
                request = request,
                currentCount = currentCount + 1,
                maxCount = maxCount,
                retryDelayMs = retryDelayMs,
                onSuccess = onSuccess,
                onFailure = onFailure,
            )
        }
    }

}
