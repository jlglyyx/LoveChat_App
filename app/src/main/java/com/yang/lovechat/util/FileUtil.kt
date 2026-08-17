package com.yang.lovechat.util

import android.content.Context
import android.provider.MediaStore
import java.io.File
import java.text.DecimalFormat
import kotlin.collections.isEmpty

/**
 * @return 格式化文件大小格式
 */
fun formatSize(size: Long): String {

    val k = size.toFloat() / 1024
    if (k < 1) {
        return "0K"
    }
    val m = k / 1024

    if (m < 1) {
        return DecimalFormat("0.00K").format(k)
    }

    val g = m / 1024

    if (g < 1) {
        return DecimalFormat("0.00M").format(m)
    }

    val t = g / 1024

    if (t < 1) {
        return DecimalFormat("0.00G").format(g)
    }
    val t1 = t / 1024

    if (t1 < 1) {
        return DecimalFormat("0.00G").format(t)
    }
    return DecimalFormat("0.00T").format(t1)
}

/**
 * @return 获取指定文件大小
 */
fun getAllFileSize(file: File?): Long {

    if (null == file) {
        return 0L
    }
    var size = 0L
    if (file.isDirectory) {
        val listFiles = file.listFiles()
        listFiles?.let {
            if (listFiles.isEmpty()) {
                return 0
            }
            for (mFile in listFiles) {
                size += if (mFile.isDirectory) {
                    getAllFileSize(mFile)
                } else {
                    mFile.length()
                }
            }
        }
    } else {
        size = file.length()
    }
    return size
}

/**
 * @return 删除文件夹
 */
fun deleteDirectory(file: File?, context: Context) {
    try {
        if (null == file) {
            return
        }
        if (file.isDirectory) {
            if (null != file.listFiles() && file.listFiles()!!.isNotEmpty()) {
                file.listFiles()?.let {
                    if (it.isNotEmpty()) {
                        for (mFile in it) {
                            if (mFile.isDirectory) {
                                deleteDirectory(mFile, context)
                            } else {
                                toDeleteFile(mFile, context)
                            }
                        }
                    } else {
                        toDeleteFile(file, context)
                    }
                }
            } else {
                toDeleteFile(file, context)
            }

        } else {
            toDeleteFile(file, context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

/**
 * @return 删除文件
 */
fun toDeleteFile(file: File, context: Context) {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val contentResolver = context.contentResolver
    val url = MediaStore.Images.Media.DATA + "=?"
    val delete = contentResolver.delete(uri, url, arrayOf(file.absolutePath))
    if (delete == 0) {
        if (file.exists()) {
            file.delete()
        }
    }
}