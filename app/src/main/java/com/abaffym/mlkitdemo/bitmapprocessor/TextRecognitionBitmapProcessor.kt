package com.abaffym.mlkitdemo.bitmapprocessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.text.FirebaseVisionText

class TextRecognitionBitmapProcessor {

    private val rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }

    fun draw(bitmap: Bitmap, visionText: FirebaseVisionText): Bitmap {
        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

       visionText.textBlocks.forEach {blocks ->
            blocks.lines.forEach { line ->
                canvas.withSave {
                    drawBoundingBox(canvas, line)
                    drawText(canvas, line)
                }
            }
        }
        return temporaryBitmap
    }

    private fun drawBoundingBox(canvas: Canvas, text: FirebaseVisionText.Line) {
        canvas.drawRoundRect(RectF(text.boundingBox), 0f, 0f, rectPaint)
    }

    private fun drawText(canvas: Canvas, text: FirebaseVisionText.Line) {
        canvas.drawText(text.text, text.boundingBox!!.left.toFloat(), text.boundingBox!!.bottom.toFloat(), textPaint)
    }


    companion object {
        private const val TEXT_COLOR = Color.RED
        private const val TEXT_SIZE = 140.0f
        private const val STROKE_WIDTH = 12.0f
    }

}