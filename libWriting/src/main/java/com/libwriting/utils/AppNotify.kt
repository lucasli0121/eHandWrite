package com.libwriting.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.write.libwriting.R

class AppNotify(context: Context) {
    private val context:Context = context
    private val channelName: String = "PUSH_NOTIFY_NAME"

    private fun createBuilder(channelId: Int, content: String?): NotificationCompat.Builder? {
        var builder: NotificationCompat.Builder? = null
        var notifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(!notifyMgr.areNotificationsEnabled()) {
            return builder
        }
        try {
            builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId.toString(),
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.enableVibration(true)
                notifyMgr.createNotificationChannel(channel)
                NotificationCompat.Builder(context, channelId.toString())
            } else {
                NotificationCompat.Builder(context)
            }
            builder!!.setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.resources.getString(R.string.notify_title))
                .setAutoCancel(true)
                .setContentText(content).priority = NotificationCompat.PRIORITY_DEFAULT

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return builder
    }
    fun makeNotify(channelId: Int, content: String?) {
        var builder = createBuilder(channelId, content)
        with(NotificationManagerCompat.from(context)) {
            builder?.build()?.let { this.notify(channelId, it) }
        }
    }

    fun notifyProgress(channelId: Int, curVal: Int, totalVal: Int) {
        var builder = createBuilder(channelId, "上传文件")
        builder?.setProgress(totalVal, curVal, false)
        with(NotificationManagerCompat.from(context)) {
            builder?.build()?.let { this.notify(channelId, it) }
        }
    }

    fun delNotify(channelId: Int) {
        try {
            var notifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(!notifyMgr.areNotificationsEnabled()) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notifyMgr.deleteNotificationChannel(channelId.toString())
            }
            with(NotificationManagerCompat.from(context)) {
                this.deleteNotificationChannel(channelId.toString())
                this.cancel(channelId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}