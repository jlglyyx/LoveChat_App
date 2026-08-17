package com.yang.lovechat.helper

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.bumptech.glide.Glide
import com.yang.lovechat.app.BaseApplication
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToLong

object MediaHelper {


    const val MAX_VIDEO_SIZE = 100 * 1024 * 1024L

    const val MAX_IMAGE_SIZE = 10 * 1024 * 1024L

    /**
     * 提取视频封面并直接通过压缩逻辑处理
     */
    suspend fun saveVideoCoverToCache(context: Context, videoUri: Uri, fileName: String): File? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            if (bitmap != null) {
                // 1. 先保存原始帧到临时文件
                val tempFile = File(context.cacheDir, "$fileName")
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                // 2. 调用通用的压缩逻辑
                val compressedFile = compressFile(context, tempFile, 720, 720, 75)

                // 3. 删除临时文件
                if (tempFile.exists()) tempFile.delete()

                return@withContext compressedFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return@withContext null
    }

    /**
     * 对 Uri 进行压缩 (外部调用)
     */
    suspend fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1080, maxHeight: Int = 1920, quality: Int = 80): File? {
        val originalFile = withContext(Dispatchers.IO) {
            Glide.with(context).downloadOnly().load(uri).submit().get()
        }
        return compressFile(context, originalFile, maxWidth, maxHeight, quality)
    }


    private suspend fun compressFile(context: Context, file: File, maxWidth: Int, maxHeight: Int, quality: Int): File? = withContext(Dispatchers.IO) {
        try {
            val originalSize = file.length()

            val compressedFile = Compressor.compress(context, file) {
                resolution(maxWidth, maxHeight)
                quality(quality)
                format(Bitmap.CompressFormat.JPEG)
            }

            val compressedSize = compressedFile.length()

            val originalMb = String.format("%.2f", originalSize / 1024.0 / 1024.0)

            val compressedMb = String.format("%.2f", compressedSize / 1024.0 / 1024.0)

            val ratio = String.format("%.1f", (1.0 - compressedSize.toDouble() / originalSize) * 100)

            Log.i("TAG", "compressFile:  $originalMb MB -> $compressedMb MB (节省: $ratio%)")

            return@withContext compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }


    suspend fun buildMultipartBody(
        context: Context = BaseApplication.mApplication,
        files: MutableList<Uri>
    ): List<MultipartBody.Part> = coroutineScope {

        val deferredParts = files.map { uri ->
            async {
                val parts = mutableListOf<MultipartBody.Part>()
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                // 1. 获取准确的原始扩展名
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                val fileId = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
                val fileName = "$fileId.$extension" // 保留原始格式
                val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L

                if (mimeType.startsWith("video/")) {

                    if (fileSize > MAX_VIDEO_SIZE) {
                        throw Exception("This video is too large. Maximum allowed size is 100 MB.")
                    }

                    // 视频：保留原始格式上传，封面单独提取
                    parts.add(MediaHelper.createFilePart("files", fileName, uri, mimeType.toMediaTypeOrNull(), context))

                    val coverFile = MediaHelper.saveVideoCoverToCache(context, uri, "${fileId}_cover.jpg")
                    coverFile?.let {
                        parts.add(MultipartBody.Part.createFormData("files", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull())))
                    }
                }
                else if (mimeType.startsWith("image/")) {
                    // 图片：依然压缩，但压缩后保存为原始类型（通常 JPEG 或 PNG）
                    val compressedFile = MediaHelper.compressImage(context, uri)
                    val finalFile = compressedFile ?: Glide.with(context).downloadOnly().load(uri).submit().get()

                    if (finalFile.length() > MAX_IMAGE_SIZE) {
                        throw Exception("This image is too large. Maximum allowed size is 10 MB.")
                    }

                    parts.add(MultipartBody.Part.createFormData("files", "$fileId.$extension", finalFile.asRequestBody(mimeType.toMediaTypeOrNull())))
                }
                parts
            }
        }
        deferredParts.awaitAll().flatten()
    }

    fun createFilePart(partName: String, fileName: String, uri: Uri, mediaType: MediaType?, context: Context): MultipartBody.Part {
        val requestBody = object : RequestBody() {
            override fun contentType(): MediaType? = mediaType
            override fun contentLength(): Long =
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L

            override fun writeTo(sink: BufferedSink) {
                context.contentResolver.openInputStream(uri)?.use { it.source().use { source -> sink.writeAll(source) } }
            }
        }
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }


    fun Uri?.getVideoDuration(context: Context): Long {

        if (null == this) return 0L

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, this)
            val time =
                (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong())
                    ?: 0L

            (time / 1000.0).roundToLong()

        } catch (e: Exception) {
            e.printStackTrace(); 0L
        } finally {
            retriever.release()
        }
    }

    fun Uri.isVideo(context: Context): Boolean {
        return context.contentResolver.getType(this)?.startsWith("video/") ?: false
    }
}