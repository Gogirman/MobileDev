package com.example.downloader

import android.R
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.widget.Button
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection


class DownloaderService: IntentService("DownloaderService") {

    private var notificationManager: NotificationManager? = null
    val channelID = "com.example.downloader.noti"

    override fun onHandleIntent(intent: Intent?) {
        createNotificationChannel()
        val uriString = intent!!.extras!!.get("uri").toString()
        try {
            val url = URL(uriString)
            val connection: URLConnection = url.openConnection()
            connection.connect()

            val input: InputStream = BufferedInputStream(url.openStream(), 8192)
            val filename: String = uriString!!.split('/').last()
            var storageDir: File? = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
            val file: File = File.createTempFile(
                filename.substring(0, filename.lastIndexOf('.')),
                filename.substring(filename.lastIndexOf('.')),
                storageDir)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val output: OutputStream = file.outputStream()
                var buf = ByteArray(8192)
                while (true) {
                    val count = input.read(buf)
                    if (count == -1)
                        break
                    output.write(buf, 0, count);
                }

                output.flush();

                output.close();
                input.close()
            }
            else {
                throw Exception("Version must be higher than Q")
            }
            sendNotification("File downloaded successfully", "Success")
        } catch(e: Exception) {
            sendNotification(e.message, "Downloaded failed")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                channelID,
                "Notify For User",
                importance
            )

            channel.description = "Notify Channel"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(message: String?, title: String) {
        val notificationID = 101

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notification =
            Notification.Builder(this, channelID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setChannelId(channelID)
                .build()

            notificationManager?.notify(notificationID, notification)
        }
    }
}