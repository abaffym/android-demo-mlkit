package com.abaffym.mlkitdemo.bitmapprocessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.text.FirebaseVisionText

class LabelDetectionBitmapProcessor {

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 24.0f
    }

    fun draw(bitmap: Bitmap, labels: List<FirebaseVisionImageLabel>): Bitmap {

        if (labels.size > 5) {
            labels.subList(0, 4)
        }

        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val x = bitmap.width / 4.0f * 3
        var y = bitmap.height / 4.0f

        canvas.withSave {
            labels.forEach { label ->
                val confidence = String.format("%.2f", label.confidence)
                canvas.drawText("${label.text}: $confidence", x, y, textPaint)
                y += 26.0f
            }
        }

        return temporaryBitmap
    }

}