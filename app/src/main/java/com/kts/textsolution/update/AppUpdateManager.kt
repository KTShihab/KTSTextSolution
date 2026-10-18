package com.kts.textsolution.update

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.kts.textsolution.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manages GitHub Releases In-App Updates for KTS Text Solution.
 * Checks for new releases via GitHub API, downloads APK, and triggers installation.
 */
class AppUpdateManager private constructor() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    data class ReleaseInfo(
        var tagName: String = "",
        var title: String = "",
        var body: String = "",
        var apkUrl: String? = null,
        var apkSize: Long = 0,
        var isNewer: Boolean = false,
    )

    fun checkForUpdates(activity: Activity?, isManualCheck: Boolean) {
        if (activity == null || activity.isFinishing) return

        val progressDialog: ProgressDialog? = if (isManualCheck) {
            ProgressDialog(activity).apply {
                setTitle(activity.getString(R.string.update_checking_title))
                setMessage(activity.getString(R.string.update_checking_msg))
                setCancelable(false)
                show()
            }
        } else {
            null
        }

        val repoSlug = DEFAULT_REPO_SLUG
        val currentVersion = getCurrentVersionName(activity)

        executor.execute {
            try {
                val apiUrl = "https://api.github.com/repos/$repoSlug/releases/latest"
                val url = URL(apiUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "KTS-Text-Solution")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val responseCode = conn.responseCode

                if (responseCode == 404) {
                    mainHandler.post {
                        progressDialog?.dismiss()
                        if (isManualCheck && !activity.isFinishing) {
                            Toast.makeText(activity, activity.getString(R.string.update_no_release), Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@execute
                }

                if (responseCode != 200) {
                    mainHandler.post {
                        progressDialog?.dismiss()
                        if (isManualCheck && !activity.isFinishing) {
                            val msg = activity.getString(R.string.update_failed, "HTTP $responseCode")
                            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                    return@execute
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                val tagName = json.optString("tag_name", "")
                val releaseTitle = json.optString("name", tagName)
                val releaseBody = json.optString("body", "Bug fixes and improvements.")

                val assets: JSONArray? = json.optJSONArray("assets")
                var apkDownloadUrl: String? = null
                var apkSize: Long = 0

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.lowercase().endsWith(".apk")) {
                            apkDownloadUrl = asset.optString("browser_download_url", null)
                            apkSize = asset.optLong("size", 0)
                            break
                        }
                    }
                }

                val info = ReleaseInfo(
                    tagName = tagName,
                    title = releaseTitle,
                    body = releaseBody,
                    apkUrl = apkDownloadUrl,
                    apkSize = apkSize,
                    isNewer = isVersionNewer(tagName, currentVersion),
                )

                mainHandler.post {
                    progressDialog?.dismiss()
                    if (activity.isFinishing) return@post

                    if (info.isNewer) {
                        showUpdateAvailableDialog(activity, info, currentVersion)
                    } else {
                        if (isManualCheck) {
                            val title = activity.getString(R.string.update_latest_title)
                            val message = activity.getString(R.string.update_latest_msg, currentVersion, tagName)
                            AlertDialog.Builder(activity)
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                            announceAccessibility(activity, "$title. $message")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                mainHandler.post {
                    progressDialog?.dismiss()
                    if (isManualCheck && !activity.isFinishing) {
                        val msg = activity.getString(R.string.update_failed, e.localizedMessage ?: "Unknown error")
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showUpdateAvailableDialog(activity: Activity, info: ReleaseInfo, currentVersion: String) {
        val title = activity.getString(R.string.update_available_title, info.tagName)
        val sizeStr = if (info.apkSize > 0) formatFileSize(info.apkSize) else "N/A"
        val message = activity.getString(R.string.update_available_msg, currentVersion, info.tagName, sizeStr, info.body)

        val dialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(activity.getString(R.string.update_download_install)) { _, _ ->
                val apkUrl = info.apkUrl
                if (!apkUrl.isNullOrEmpty()) {
                    downloadAndInstallApk(activity, apkUrl, info.tagName)
                } else {
                    Toast.makeText(activity, activity.getString(R.string.update_no_release), Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(activity.getString(R.string.update_later), null)
            .create()

        dialog.show()
        announceAccessibility(activity, "$title. $currentVersion to ${info.tagName}")
    }

    fun downloadAndInstallApk(activity: Activity, downloadUrl: String, versionName: String) {
        if (activity.isFinishing) return

        val downloadDialog = ProgressDialog(activity).apply {
            setTitle(activity.getString(R.string.update_downloading_title))
            setMessage(activity.getString(R.string.update_downloading_msg, versionName))
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            progress = 0
            setCancelable(false)
            show()
        }

        executor.execute {
            var targetApk: File? = null
            try {
                var currentUrl = downloadUrl
                var conn: HttpURLConnection? = null
                var redirects = 0
                val maxRedirects = 8

                while (redirects < maxRedirects) {
                    val url = URL(currentUrl)
                    conn = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("User-Agent", "KTS-Text-Solution")
                        instanceFollowRedirects = true
                    }
                    conn.connect()

                    val status = conn.responseCode
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == 307 || status == 308
                    ) {
                        val newUrl = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (newUrl != null) {
                            currentUrl = newUrl
                            redirects++
                            continue
                        }
                    }
                    break
                }

                val activeConn = conn ?: throw IllegalStateException("Could not establish connection")
                val fileLength = activeConn.contentLength
                val downloadDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: activity.cacheDir
                val sanitizedVersion = versionName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                targetApk = File(downloadDir, "KTSTextSolution-$sanitizedVersion.apk")

                val input: InputStream = activeConn.inputStream
                val output = FileOutputStream(targetApk)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastProgressTime: Long = 0

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    val now = System.currentTimeMillis()
                    if (fileLength > 0 && now - lastProgressTime > 150) {
                        lastProgressTime = now
                        val progressPercent = (total * 100 / fileLength).toInt()
                        mainHandler.post { downloadDialog.progress = progressPercent }
                    }
                }

                output.flush()
                output.close()
                input.close()
                activeConn.disconnect()

                val finalFile = targetApk
                mainHandler.post {
                    if (downloadDialog.isShowing) downloadDialog.dismiss()
                    installApk(activity, finalFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                mainHandler.post {
                    if (downloadDialog.isShowing) downloadDialog.dismiss()
                    Toast.makeText(activity, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun installApk(activity: Activity, apkFile: File?) {
        if (activity.isFinishing || apkFile == null || !apkFile.exists()) {
            Toast.makeText(activity, "APK file not found.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.packageManager.canRequestPackageInstalls()) {
                    AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.update_permission_title))
                        .setMessage(activity.getString(R.string.update_permission_msg))
                        .setPositiveButton(activity.getString(R.string.update_go_to_settings)) { _, _ ->
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = Uri.parse("package:${activity.packageName}")
                            }
                            activity.startActivity(intent)
                        }
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .show()
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apkFile,
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install launch failed", e)
            Toast.makeText(activity, "Install error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun announceAccessibility(activity: Activity, text: String) {
        try {
            activity.window?.decorView?.announceForAccessibility(text)
        } catch (ignored: Exception) {}
    }

    companion object {
        private const val TAG = "AppUpdateManager"
        const val DEFAULT_REPO_SLUG = "KTShihab/KTSTextSolution"

        @Volatile
        private var instance: AppUpdateManager? = null

        fun getInstance(): AppUpdateManager =
            instance ?: synchronized(this) {
                instance ?: AppUpdateManager().also { instance = it }
            }

        fun getCurrentVersionName(context: Context): String {
            return try {
                val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "1.0"
            } catch (e: Exception) {
                "1.0"
            }
        }

        fun getCurrentVersionCode(context: Context): Int {
            return try {
                val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode
                }
            } catch (e: Exception) {
                1
            }
        }

        fun isVersionNewer(remoteTag: String?, currentVersion: String?): Boolean {
            if (remoteTag.isNullOrBlank() || currentVersion.isNullOrBlank()) return false

            val cleanRemote = remoteTag.replace(Regex("^[vV]"), "").trim()
            val cleanCurrent = currentVersion.replace(Regex("^[vV]"), "").trim()

            val remoteParts = cleanRemote.split(Regex("[.-]"))
            val currentParts = cleanCurrent.split(Regex("[.-]"))

            val length = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until length) {
                val remoteNum = if (i < remoteParts.size) remoteParts[i].toIntOrNull() ?: 0 else 0
                val currentNum = if (i < currentParts.size) currentParts[i].toIntOrNull() ?: 0 else 0

                if (remoteNum > currentNum) return true
                if (remoteNum < currentNum) return false
            }

            return false
        }

        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            if (bytes < 1024) return "$bytes B"
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
