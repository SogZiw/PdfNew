package com.word.file.manager.pdf.base.helper.notice

import android.content.Context
import android.view.View
import androidx.core.app.NotificationCompat

object NoticeUtils {

    // Settings.canDrawOverlays
    fun canDrawOverlaysByReflection(context: Context): Boolean {
        return runCatching {
            val settingsClass = Class.forName("android.provider.Settings")
            val method = settingsClass.getMethod("canDrawOverlays", Context::class.java)
            return method.invoke(null, context) as Boolean
        }.getOrNull() ?: false
    }

    fun addViewByReflection(
        context: Context,
        view: View?,
        marginOffsetPx: Int
    ) {
        runCatching {
            val getSystemServiceMethod = Context::class.java.getMethod("getSystemService", String::class.java)
            val windowManager = getSystemServiceMethod.invoke(context, "window")
            val width = context.resources.displayMetrics.widthPixels - marginOffsetPx
            // build WindowManager$LayoutParams
            val layoutParamsClass = Class.forName($$"android.view.WindowManager$LayoutParams")
            val constructor = layoutParamsClass.getConstructor(
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            val layoutParams = constructor.newInstance(width, -2, 2038, 552, -3)
            layoutParamsClass.getField("gravity").setInt(layoutParams, 49)
            layoutParamsClass.getField("x").setInt(layoutParams, 0)
            layoutParamsClass.getField("y").setInt(layoutParams, marginOffsetPx)
            // windowManager.addView
            val windowManagerClass = Class.forName("android.view.WindowManager")
            val addViewMethod = windowManagerClass.getMethod(
                "addView", View::class.java,
                Class.forName($$"android.view.ViewGroup$LayoutParams")
            )
            addViewMethod.invoke(windowManager, view, layoutParams)
        }
    }

    fun removeViewByReflection(
        context: Context,
        view: View?
    ) {
        if (view == null) return
        runCatching {
            val getSystemServiceMethod = Context::class.java.getMethod("getSystemService", String::class.java)
            val windowManager = getSystemServiceMethod.invoke(context, "window")
            val windowManagerClass = Class.forName("android.view.WindowManager")
            val removeViewMethod = windowManagerClass.getMethod("removeView", View::class.java)
            removeViewMethod.invoke(windowManager, view)
        }
    }


    // 设置媒体通知style
    fun setMediaStyleByReflection(
        context: Context,
        builder: NotificationCompat.Builder,
        tag: String
    ) {
        runCatching {
            val mediaSessionClass = Class.forName("android.support.v4.media.session.MediaSessionCompat")
            val constructor = mediaSessionClass.getConstructor(Context::class.java, String::class.java)
            val mediaSession = constructor.newInstance(context, tag)
            // setFlags
            val setFlagsMethod = mediaSessionClass.getMethod("setFlags", Int::class.javaPrimitiveType)
            setFlagsMethod.invoke(mediaSession, 3)
            // setActive
            val setActiveMethod = mediaSessionClass.getMethod("setActive", Boolean::class.javaPrimitiveType)
            setActiveMethod.invoke(mediaSession, true)

            // get sessionToken
            val getTokenMethod = mediaSessionClass.getMethod("getSessionToken")
            val token = getTokenMethod.invoke(mediaSession)

            // create MediaStyle
            val mediaStyleClass = Class.forName($$"androidx.media.app.NotificationCompat$MediaStyle")
            val mediaStyle = mediaStyleClass.getConstructor().newInstance()

            val tokenClass = Class.forName($$"android.support.v4.media.session.MediaSessionCompat$Token")
            val setMediaSessionMethod = mediaStyleClass.getMethod("setMediaSession", tokenClass)
            setMediaSessionMethod.invoke(mediaStyle, token)
            builder.setStyle(mediaStyle as NotificationCompat.Style)
        }
    }

    // 熄屏唤醒
    fun acquireWakeLockByReflect(context: Context, tag: String) {
        runCatching {
            val contextClass = Class.forName("android.content.Context")
            val powerServiceField = contextClass.getField("POWER_SERVICE")
            val powerService = powerServiceField.get(null) as String
            val getSystemServiceMethod = contextClass.getMethod("getSystemService", String::class.java)
            val powerManager = getSystemServiceMethod.invoke(context, powerService)
            val powerManagerClass = Class.forName("android.os.PowerManager")
            val newWakeLockMethod = powerManagerClass.getMethod(
                "newWakeLock",
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val wakeLock = newWakeLockMethod.invoke(powerManager, 268435466, tag)
            val wakeLockClass = Class.forName($$"android.os.PowerManager$WakeLock")
            val acquireMethod = wakeLockClass.getMethod("acquire", Long::class.javaPrimitiveType)
            acquireMethod.invoke(wakeLock, 3000L)
        }
    }


}