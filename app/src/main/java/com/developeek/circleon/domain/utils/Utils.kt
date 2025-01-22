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
import android.util.Log
import android.util.TimeFormatException
import android.view.MenuItem
import com.developeek.circleon.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

object Utils {
    // TODO: local properties 로 이동
    private const val CIRCLE_IMAGE_PATH = "circles/images/"
    private const val POST_IMAGE_PATH = "posts/images/"
    private const val MAX_IMAGE_WIDTH = 800f
    private const val MAX_IMAGE_HEIGHT = 800f

    fun getCircleImageUrlOrNull(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + CIRCLE_IMAGE_PATH + url
    }

    fun getPostImageUrlOrNull(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + POST_IMAGE_PATH + url
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
        val tempFile = File.createTempFile("temp", ".jpg")
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

        val scaleFactor =
            max(
                1,
                min(
                    options.outWidth.toFloat() / MAX_IMAGE_WIDTH,
                    options.outHeight.toFloat() / MAX_IMAGE_HEIGHT,
                ).toInt(),
            )

        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        val bitMap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        Log.d("bitMap", "${bitMap.width}, ${bitMap.height}")

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
