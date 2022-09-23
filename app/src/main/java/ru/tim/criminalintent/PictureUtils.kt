package ru.tim.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import kotlinx.coroutines.supervisorScope
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = activity.windowManager.currentWindowMetrics.bounds
        getScaledBitmap(path, bounds.width(), bounds.height())
    } else {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        getScaledBitmap(path, size.x, size.y)
    }
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    //Чтение размеров изображения на диске
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //Выясняем, на сколько нужно уменьшить
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale) heightScale else widthScale
        inSampleSize = sampleScale.roundToInt()
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    //Чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}