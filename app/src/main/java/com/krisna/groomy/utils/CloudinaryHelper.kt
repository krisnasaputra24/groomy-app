package com.krisna.groomy.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryHelper {
    
    // Konfigurasi Cloudinary Anda
    private const val CLOUD_NAME = "dfi82tgyp" // Ganti dengan Cloud Name Anda jika berbeda
    private const val UPLOAD_PRESET = "groomy_preset" // Ganti dengan Upload Preset (Unsigned) Anda

    fun init(context: Context) {
        try {
            val config = mapOf(
                "cloud_name" to CLOUD_NAME,
                "secure" to true
            )
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // MediaManager sudah diinisialisasi
        }
    }

    suspend fun uploadImage(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    continuation.resume(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resume(null)
                }
            }).dispatch()
    }
}
