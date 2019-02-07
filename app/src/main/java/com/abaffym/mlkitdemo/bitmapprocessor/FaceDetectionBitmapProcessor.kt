package com.abaffym.mlkitdemo.bitmapprocessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.face.FirebaseVisionFace

class FaceDetectionBitmapProcessor {

	fun drawBoundingBoxes(bitmap: Bitmap, visionFaces: List<FirebaseVisionFace>): Bitmap {
		val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

		val canvas = Canvas(temporaryBitmap)
		canvas.drawBitmap(bitmap, 0f, 0f, null)

		for (face in visionFaces) {
			canvas.withSave {
				drawRoundRect(RectF(face.boundingBox), 0f, 0f, rectPaint)
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