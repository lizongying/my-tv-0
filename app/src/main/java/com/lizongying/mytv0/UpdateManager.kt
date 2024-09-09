package com.lizongying.mytv0


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.lizongying.mytv0.requests.HttpClient
import com.lizongying.mytv0.requests.ReleaseRequest
import com.lizongying.mytv0.requests.ReleaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class UpdateManager(
    private val context: Context,
    private val versionCode: Long
) : ConfirmationFragment.ConfirmationListener {

    private var releaseRequest = ReleaseRequest()
    private var release: ReleaseResponse? = null
    private val okHttpClient = HttpClient.okHttpClient
    private var downloadJob: Job? = null
    private var lastLoggedProgress = -1

    fun checkAndUpdate() {
        Log.i(TAG, "checkAndUpdate")
        CoroutineScope(Dispatchers.Main).launch {
            var text = "版本获取失败"
            var update = false
            try {
                release = releaseRequest.getRelease()
                Log.i(TAG, "versionCode $versionCode ${release?.version_code}")
                if (release?.version_code != null) {
                    if (release?.version_code!! >= versionCode) {
                        text = "最新版本：${release?.version_name}"
                        update = true
                    } else {
                        text = "已是最新版本，不需要更新"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred: ${e.message}", e)
            }
            updateUI(text, update)
        }
    }

    private fun updateUI(text: String, update: Boolean) {
        val dialog = ConfirmationFragment(this@UpdateManager, text, update)
        dialog.show((context as FragmentActivity).supportFragmentManager, TAG)
    }

    private fun startDownload(release: ReleaseResponse) {
        val apkName = "my-tv-0"
        val apkFileName = "$apkName-${release.version_name}${APK_SUFFIX}.apk"
        val v = release.version_name?.removePrefix("v")
        val url =
            "${HttpClient.DOWNLOAD_HOST}${release.version_name}${APK_SUFFIX}/$apkName.${v}${APK_SUFFIX}.apk"
        Log.i(
            TAG,
            "url ${url}"
        )
        var downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir == null) {
            downloadDir = File(context.filesDir, "downloads")
        }
        cleanupDownloadDirectory(downloadDir, apkName)
        val file = File(downloadDir, apkFileName)
        file.parentFile?.mkdirs()
        Log.i(TAG, "save dir ${file}")
        downloadJob = GlobalScope.launch(Dispatchers.IO) {
            downloadWithRetry(url, file)
        }
    }

    private fun cleanupDownloadDirectory(directory: File?, apkNamePrefix: String) {
        directory?.let { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.name.startsWith(apkNamePrefix) && file.name.endsWith(".apk")) {
                    val deleted = file.delete()
                    if (deleted) {
                        Log.i(TAG, "Deleted old APK file: ${file.name}")
                    } else {
                        Log.e(TAG, "Failed to delete old APK file: ${file.name}")
                    }
                }
            }
        }
    }

    private suspend fun downloadWithRetry(url: String, file: File, maxRetries: Int = 3) {
        var retries = 0
        while (retries < maxRetries) {
            try {
                downloadFile(url, file)
                // If download is successful, break the loop
                break
            } catch (e: IOException) {
                Log.e(TAG, "Download failed: ${e.message}")
                retries++
                if (retries >= maxRetries) {
                    Log.e(TAG, "Max retries reached. Download failed.")
                    withContext(Dispatchers.Main) {
                        // Notify user about download failure
                        updateUI("下载失败，请检查网络连接后重试", false)
                    }
                } else {
                    Log.i(TAG, "Retrying download (${retries}/${maxRetries})")
                    delay(30000) // Wait for 30 seconds before retrying
                }
            }
        }
    }

    private suspend fun downloadFile(url: String, file: File) {
        val request = okhttp3.Request.Builder().url(url)
            .addHeader("Accept", "application/vnd.android.package-archive").build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val body = response.body() ?: throw IOException("Null response body")
        val actualMimeType = body.contentType()?.toString()
        if (actualMimeType != "application/vnd.android.package-archive") {
            throw IOException("Unexpected MIME type: $actualMimeType. Expected: application/vnd.android.package-archive")
        }
        val contentLength = body.contentLength()
        var bytesRead = 0L

        body.byteStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytes: Int
                while (inputStream.read(buffer).also { bytes = it } != -1) {
                    outputStream.write(buffer, 0, bytes)
                    bytesRead += bytes
                    val progress =
                        if (contentLength > 0) (bytesRead * 100 / contentLength).toInt() else -1
                    withContext(Dispatchers.Main) {
                        updateDownloadProgress(progress)
                    }
                }
            }
        }

        withContext(Dispatchers.Main) {
            installNewVersion(file)
        }
    }

    private fun updateDownloadProgress(progress: Int) {
        if (progress == -1) {
            // Log when progress can't be determined
            Log.i(TAG, "Download in progress, size unknown")
        } else if (progress % 10 == 0 && progress != lastLoggedProgress) {
            // Log every 10% and avoid duplicate logs
            Log.i(TAG, "Download progress: $progress%")
            lastLoggedProgress = progress
            "升级文件已经下载：${progress}%".showToast()
        }
    }

    private fun installNewVersion(apkFile: File) {
        if (apkFile.exists()) {
            val apkUri = Uri.fromFile(apkFile) // Use Uri.fromFile for Android 4.4
            Log.i(TAG, "apkUri $apkUri")
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } else {
            Log.e(TAG, "APK file does not exist!")
        }
    }

    companion object {
        private const val TAG = "UpdateManager"
        private const val BUFFER_SIZE = 8192
        private const val APK_SUFFIX = "-kitkat"
    }

    override fun onConfirm() {
        Log.i(TAG, "onConfirm $release")
        release?.let { startDownload(it) }
    }

    override fun onCancel() {
    }

    fun destroy() {
        downloadJob?.cancel()
    }
}