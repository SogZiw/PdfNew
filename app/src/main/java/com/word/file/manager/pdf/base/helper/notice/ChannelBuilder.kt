package com.word.file.manager.pdf.base.helper.notice

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.edit
import com.word.file.manager.pdf.app
import kotlin.random.Random

object ChannelBuilder {

    private const val STORE_NAME = "pdf_notice_channel_state"
    private const val KEY_CREATED_CHANNEL_IDS = "createdDynamicChannelIds"
    private const val KEY_LAST_CREATE_TIME = "lastDynamicChannelCreateTime"
    private const val KEY_LAST_CHANNEL_ID = "lastDynamicChannelId"
    private const val SUFFIX_LETTERS = "abcdefghijklmnopqrstuvwxyz"

    private val channelState by lazy {
        app.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    }

    fun createDynamicChannelIfNeeded(
        createIntervalMillis: Long,
        maxDynamicChannelCount: Int,
        baseChannelId: String,
    ): String {
        val lastChannelId = getLastChannelId(baseChannelId)
        if (!isNotificationChannelDisabled(lastChannelId)) return lastChannelId
        if (lastChannelId == baseChannelId && !hasCreateTime()) {
            recordLastCreateTime()
            return lastChannelId
        }
        if (!hasPassedCreateInterval(createIntervalMillis)) return lastChannelId
        if (!hasChannelQuota(baseChannelId, maxDynamicChannelCount)) return lastChannelId
        val channelId = buildDynamicChannelId(baseChannelId) ?: return lastChannelId
        deleteNotificationChannel(lastChannelId)
        recordCreatedChannel(channelId)
        return channelId
    }

    private fun getLastChannelId(baseChannelId: String): String {
        val lastChannelId = channelState.getString(KEY_LAST_CHANNEL_ID, null).orEmpty()
        return lastChannelId.takeIf { it.startsWith(baseChannelId) } ?: baseChannelId
    }

    private fun hasCreateTime(): Boolean {
        return channelState.getLong(KEY_LAST_CREATE_TIME, 0L) > 0L
    }

    private fun isNotificationChannelDisabled(channelId: String): Boolean {
        val manager = app.getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(channelId)
        return channel?.importance == NotificationManager.IMPORTANCE_NONE
    }

    private fun deleteNotificationChannel(channelId: String) {
        val manager = app.getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(channelId)
    }

    private fun hasPassedCreateInterval(createIntervalMillis: Long): Boolean {
        val lastCreateTime = channelState.getLong(KEY_LAST_CREATE_TIME, 0L)
        if (lastCreateTime <= 0L) return true
        return System.currentTimeMillis() - lastCreateTime >= createIntervalMillis.coerceAtLeast(0L)
    }

    private fun hasChannelQuota(baseChannelId: String, maxDynamicChannelCount: Int): Boolean {
        if (maxDynamicChannelCount <= 0) return false
        return collectDynamicChannelIds(baseChannelId).size < maxDynamicChannelCount
    }

    private fun buildDynamicChannelId(baseChannelId: String): String? {
        val existingIds = collectKnownChannelIds() + collectRecordedChannelIds()
        val availableIds = buildList {
            for (first in SUFFIX_LETTERS) {
                for (second in SUFFIX_LETTERS) {
                    val candidate = "${baseChannelId}_${first}${second}"
                    if (candidate !in existingIds) add(candidate)
                }
            }
        }
        if (availableIds.isEmpty()) return null
        return availableIds[Random.nextInt(availableIds.size)]
    }

    private fun recordCreatedChannel(channelId: String) {
        val dynamicChannelIds = channelState.getStringSet(KEY_CREATED_CHANNEL_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { add(channelId) }
        channelState.edit {
            putLong(KEY_LAST_CREATE_TIME, System.currentTimeMillis())
            putString(KEY_LAST_CHANNEL_ID, channelId)
            putStringSet(KEY_CREATED_CHANNEL_IDS, dynamicChannelIds)
        }
    }

    private fun recordLastCreateTime() {
        channelState.edit {
            putLong(KEY_LAST_CREATE_TIME, System.currentTimeMillis())
        }
    }

    private fun collectDynamicChannelIds(baseChannelId: String): Set<String> {
        val recordedIds = collectRecordedChannelIds()
        val systemIds = collectKnownChannelIds()
        return (recordedIds + systemIds).filterTo(mutableSetOf()) {
            it.isDynamicChannelId(baseChannelId)
        }
    }

    private fun collectRecordedChannelIds(): Set<String> {
        return channelState.getStringSet(KEY_CREATED_CHANNEL_IDS, emptySet())
            .orEmpty()
            .toSet()
    }

    private fun collectKnownChannelIds(): Set<String> {
        val manager = app.getSystemService(NotificationManager::class.java)
        return manager.notificationChannels.mapTo(mutableSetOf()) { it.id }
    }

    private fun String.isDynamicChannelId(baseChannelId: String): Boolean {
        if (!startsWith("${baseChannelId}_")) return false
        val suffix = removePrefix("${baseChannelId}_")
        return suffix.length == 2 && suffix.all { it in SUFFIX_LETTERS }
    }
}
