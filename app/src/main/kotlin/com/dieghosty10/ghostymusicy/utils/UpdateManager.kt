package com.dieghosty10.ghostymusicy.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

object UpdateManager {
    val downloadProgress = MutableStateFlow<Float?>(null)

    fun downloadAndInstallUpdate(context: Context, url: String, versionTag: String) {
        val fileName = "GhostyMusic-$versionTag.apk"
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Descargando Actualización")
            setDescription("Descargando $fileName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(context, "Descarga iniciada...", Toast.LENGTH_SHORT).show()

        downloadProgress.value = 0f
        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0 && statusIndex >= 0) {
                        val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                        val bytesTotal = cursor.getLong(bytesTotalIndex)
                        val status = cursor.getInt(statusIndex)

                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            downloading = false
                            if (status == DownloadManager.STATUS_FAILED) {
                                downloadProgress.value = null
                            } else {
                                downloadProgress.value = 1f
                            }
                        } else {
                            if (bytesTotal > 0) {
                                downloadProgress.value = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                            }
                        }
                    } else {
                        downloading = false
                        downloadProgress.value = null
                    }
                } else {
                    downloading = false
                    downloadProgress.value = null
                }
                cursor?.close()
                delay(100)
            }
        }

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val downloadedUri = downloadManager.getUriForDownloadedFile(downloadId)
                    if (downloadedUri != null) {
                        installApk(ctx, downloadedUri)
                    } else {
                        Toast.makeText(ctx, "Error al descargar la actualización", Toast.LENGTH_SHORT).show()
                    }
                    try {
                        ctx.unregisterReceiver(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Register receiver (Android 13+ requires RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED, but DOWNLOAD_MANAGER is system so NOT_EXPORTED is fine, actually it sends global intent)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se pudo iniciar el instalador", Toast.LENGTH_SHORT).show()
        }
    }
}
