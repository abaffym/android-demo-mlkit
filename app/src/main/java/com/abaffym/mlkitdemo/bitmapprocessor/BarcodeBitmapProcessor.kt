package com.abaffym.mlkitdemo.bitmapprocessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

class BarcodeBitmapProcessor {

	fun drawBoundingBoxes(bitmap: Bitmap, visionBarcodes: List<FirebaseVisionBarcode>): Bitmap {
		val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

		val canvas = Canvas(temporaryBitmap)
		canvas.drawBitmap(bitmap, 0f, 0f, null)

		for (barcode in visionBarcodes) {
			canvas.withSave {
				drawRoundRect(RectF(barcode.boundingBox), 0f, 0f, rectPaint)
			}
		}
		return temporaryBitmap
	}

	private val rectPaint = Paint().apply {
		strokeWidth = 5f
		color = Color.CYAN
		style = Paint.Style.STROKE
	}

}