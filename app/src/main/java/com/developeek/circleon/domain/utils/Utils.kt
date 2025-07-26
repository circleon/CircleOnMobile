package com.developeek.circleon.domain.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TimeFormatException
import android.view.MenuItem
import com.developeek.circleon.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import kotlin.math.max

object Utils {
    private const val MAX_IMAGE_WIDTH = 800f

    fun getCircleImageUrlOrNull(url: String?) =
        url?.let {
            BuildConfig.SERVICE_IMAGE_API_URL + BuildConfig.CIRCLE_IMAGE_PATH + it
        }

    fun getPostImageUrlOrNull(url: String?) =
        url?.let {
            BuildConfig.SERVICE_IMAGE_API_URL + BuildConfig.POST_IMAGE_PATH + it
        }

    fun getUserImageUrlOrNull(url: String?) =
        url?.let {
            BuildConfig.SERVICE_IMAGE_API_URL + BuildConfig.USER_IMAGE_PATH + it
        }

    fun getLocalDateTimeOrDefault(dateTime: String): LocalDateTime {
        val default = LocalDateTime.of(1, 1, 1, 1, 1)

        return try {
            LocalDateTime.parse(dateTime)
        } catch (e: TimeFormatException) {
            default
        }
    }

    fun changeMenuItemTextColor(
        item: MenuItem,
        color: Int,
    ) {
        val title = SpannableString(item.title)
        item.title =
            title.apply {
                setSpan(
                    ForegroundColorSpan(color),
                    0,
                    length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE,
                )
            }
    }

    fun Uri.toJPEG(context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(this)
        val tempFile = File.createTempFile("temp", ".jpeg")
        return try {
            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }
            tempFile.deleteOnExit()
            inputStream?.close()
            resizeImageFile(context, tempFile)
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeImageFile(
        context: Context,
        file: File,
    ): File? {
        val options =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
        BitmapFactory.decodeFile(file.absolutePath, options)

        // 압축 배수는 최소 1 이상(원본보다 해상도를 크게 x), 원본의 가로 해상도 기준으로 결정
        val scaleFactor = max(1, (options.outWidth.toFloat() / MAX_IMAGE_WIDTH).toInt())

        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        val bitMap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

        val matrix = Matrix()
        val exif = ExifInterface(file)
        val rotation =
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        if (rotation != 0) {
            matrix.postRotate(rotation.toFloat())
        }

        val rotatedBitMap = Bitmap.createBitmap(bitMap, 0, 0, bitMap.width, bitMap.height, matrix, true)

        return saveBitmapAsFile(context, rotatedBitMap, file.name)
    }

    private fun saveBitmapAsFile(
        context: Context,
        bitmap: Bitmap?,
        fileName: String,
    ): File? {
        val file = File(context.cacheDir, fileName)

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
