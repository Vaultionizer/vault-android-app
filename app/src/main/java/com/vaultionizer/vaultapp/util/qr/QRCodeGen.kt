package com.vaultionizer.vaultapp.util.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.lang.Exception


object QRCodeGen {
    fun generateQRCode(authKey: String) : Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val matrix = writer.encode(authKey, BarcodeFormat.QR_CODE, 1024, 1024)
            val width = matrix.width
            val height = matrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width){
                for (y in 0 until height){
                    bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE )
                }
            }
            bitmap
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}