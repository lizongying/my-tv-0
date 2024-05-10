package com.lizongying.mytv0

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Hashtable

class QrCodeUtil {

    fun createQRCodeBitmap(
        content: String,
        width: Int,
        height: Int,
        characterSet: String = "UTF-8",
        errorCorrection: String = "L",
        margin: String = "1",
        @ColorInt colorBlack: Int = Color.BLACK,
        @ColorInt colorWhite: Int = Color.WHITE,
    ): Bitmap? {
        if (width < 0 || height < 0) {
            return null
        }
        try {
            val hints: Hashtable<EncodeHintType, String> = Hashtable()
            if (characterSet.isNotEmpty()) {
                hints[EncodeHintType.CHARACTER_SET] = characterSet
            }
            if (errorCorrection.isNotEmpty()) {
                hints[EncodeHintType.ERROR_CORRECTION] = errorCorrection
            }
            if (margin.isNotEmpty()) {
                hints[EncodeHintType.MARGIN] = margin
            }
            val bitMatrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (bitMatrix[x, y]) {
                        pixels[y * width + x] = colorBlack
                    } else {
                        pixels[y * width + x] = colorWhite
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }
}