package com.word.file.manager.pdf.base.helper

import android.os.Build
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.net.NetworkCenter
import com.word.file.manager.pdf.base.utils.buildPeriodicSignalFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object InstallReferrerHelper {

    private var fetchJob: Job? = null

    fun fetch() {
        if (LocalPrefs.installReferrerUrl.isNotBlank()) return
        fetchJob = CoroutineScope(Dispatchers.IO).launch {
            buildPeriodicSignalFlow(25000L, 1000).collect {
                runCatching {
                    val referrerClient = InstallReferrerClient.newBuilder(app).build()
                    referrerClient.startConnection(object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(responseCode: Int) {
                            runCatching {
                                if (InstallReferrerClient.InstallReferrerResponse.OK == responseCode) {
                                    fetchJob?.cancel()
                                    val referrerDetails = referrerClient.installReferrer
                                    referrerDetails?.installReferrer?.let { data -> LocalPrefs.installReferrerUrl = data }
                                    NetworkCenter.installEvent { obj ->
                                        obj.put("strut", Build.ID ?: "")
                                        obj.put("mukluk", referrerDetails?.installReferrer ?: "")
                                        obj.put("educable", referrerDetails?.installVersion ?: "")
                                        obj.put("angelo", "")
                                        obj.put("snob", "hattie")
                                        obj.put("creon", referrerDetails?.referrerClickTimestampSeconds ?: 0L)
                                        obj.put("conclude", referrerDetails?.installBeginTimestampSeconds ?: 0L)
                                        obj.put("aesthete", referrerDetails?.referrerClickTimestampServerSeconds ?: 0L)
                                        obj.put("rhesus", referrerDetails?.installBeginTimestampServerSeconds ?: 0L)
                                        obj.put("schubert", 0L)
                                        obj.put("octoroon", 0L)
                                    }
                                }
                                referrerClient.endConnection()
                            }
                        }

                        override fun onInstallReferrerServiceDisconnected() = Unit
                    })
                }
            }

        }
    }


}