package com.abaffym.mlkitdemo.bitmapprocessor

import android.graphics.*
import androidx.core.graphics.withSave
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark

class FaceDetectionBitmapProcessor {

    companion object {
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 20.0f
    }

    private val facePositionPaint = Paint().apply {
        color = Color.WHITE
    }

    private val idPaint = Paint().apply {
        color = Color.BLUE
        textSize = ID_TEXT_SIZE
    }

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    fun draw(bitmap: Bitmap, visionFaces: List<FirebaseVisionFace>): Bitmap {
        val temporaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(temporaryBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        canvas.withSave {
            visionFaces.forEach { face ->
                drawBoundingBox(canvas, face)
                drawLandmarks(canvas, face)
                drawContours(canvas, face)
            }
        }
        return temporaryBitmap
    }

    private fun drawBoundingBox(canvas: Canvas, face: FirebaseVisionFace) {
        canvas.drawRoundRect(RectF(face.boundingBox), 0f, 0f, boxPaint)
    }

    private fun drawLandmarks(canvas: Canvas, face: FirebaseVisionFace) {
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_BOTTOM)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_LEFT)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.NOSE_BASE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_RIGHT)
    }

    private fun drawLandmarkPosition(canvas: Canvas, face: FirebaseVisionFace, landmarkID: Int) {
        val landmark = face.getLandmark(landmarkID)
        landmark?.let {
            val point = it.position
            canvas.drawCircle(point.x, point.y, 25f, idPaint)
        }
    }

    private fun drawContours(canvas: Canvas, face: FirebaseVisionFace) {
        val contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS)
        for (point in contour.points) {
            val px = point.x
            val py = point.y
            canvas.drawCircle(px, py, 10f, facePositionPaint)
        }

    }

}