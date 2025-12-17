package com.example.findu.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {
    // 创建临时图片文件
    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    // 启动相机
    fun launchCamera(context: Context, cameraLauncher: ActivityResultLauncher<Uri>): File {
        val imageFile = createTempImageFile(context)
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        cameraLauncher.launch(imageUri)
        return imageFile
    }

    // 压缩图片（<150KB）
    fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var quality = 100
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            while (file.length() > 150 * 1024 && quality > 10) {
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
        } finally {
            outputStream?.close()
            bitmap.recycle()
        }
        return file
    }

    // 抹除EXIF信息（简化实现：重写图片）
    fun eraseExif(imageFile: File): File {
        val bitmap = BitmapFactory.decodeFile(imageFile.path)
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        bitmap.recycle()
        return imageFile
    }
}