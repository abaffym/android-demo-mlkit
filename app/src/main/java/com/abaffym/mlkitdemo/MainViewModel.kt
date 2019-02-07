package com.abaffym.mlkitdemo

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abaffym.mlkitdemo.bitmapprocessor.BarcodeBitmapProcessor
import com.abaffym.mlkitdemo.bitmapprocessor.FaceDetectionBitmapProcessor
import com.abaffym.mlkitdemo.bitmapprocessor.TextRecognitionBitmapProcessor
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

private const val TAG = "MainViewModel"

sealed class Error {
	data class StringError(val message: String) : Error()
	data class ResError(val errorId: Int) : Error()
}

sealed class Result {
	object Progress : Result()
	data class Success(val bitmap: Bitmap? = null, val text: String?) : Result()
	data class Fail(val error: Error) : Result()
}

enum class Feature {
	FACE_DETECTION, TEXT_RECOGNITION, BARCODE_SCANNING, LABEL_DETECTION
}

val features = listOf(
	Feature.LABEL_DETECTION to R.string.btn_label_detection,
	Feature.FACE_DETECTION to R.string.btn_face_detection,
	Feature.BARCODE_SCANNING to R.string.btn_barcode_scanning,
	Feature.TEXT_RECOGNITION to R.string.btn_text_recognition
)

class MainViewModel : ViewModel() {

	private val _result = MutableLiveData<Result>()

	val result: LiveData<Result> = _result

	val bitmap: MutableLiveData<Bitmap> = MutableLiveData()

	val feature: MutableLiveData<Feature> = MutableLiveData()

	init {

		feature.value = features.first().first

		bitmap.observeForever { bitmap ->
			feature.value?.let { feature ->
				applyFeature(feature, bitmap)
			}
		}
		feature.observeForever { feature ->
			bitmap.value?.let { bitmap ->
				applyFeature(feature, bitmap)
			}
		}
	}

	private fun applyFeature(feature: Feature, bitmap: Bitmap) {
		_result.value = Result.Progress
		when (feature) {
			Feature.FACE_DETECTION -> doFaceDetection(bitmap)
			Feature.TEXT_RECOGNITION -> doTextRecognition(bitmap)
			Feature.BARCODE_SCANNING -> doBarcodeScanning(bitmap)
			Feature.LABEL_DETECTION -> doLabelDetection(bitmap)
		}
	}

	private fun doTextRecognition(bitmap: Bitmap) {

		val image = FirebaseVisionImage.fromBitmap(bitmap)

		val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

		detector.processImage(image)
			.addOnSuccessListener { firebaseVisionText ->
				// Task completed successfully
				if (firebaseVisionText.text.isNotEmpty()) {
					val resultBitmap = TextRecognitionBitmapProcessor().drawBoundingBoxes(bitmap, firebaseVisionText)
					val resultText = firebaseVisionText.text
					_result.value = Result.Success(bitmap = resultBitmap, text = resultText)
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_no_text))
				}
			}
			.addOnFailureListener { e ->
				// Task failed with an exception
				Log.e(TAG, "Error detecting faces", e)
				val errorMessage = e.message
				if (errorMessage != null) {
					_result.value = Result.Fail(Error.StringError(errorMessage))
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_unknown))
				}
			}

	}

	private fun doBarcodeScanning(bitmap: Bitmap) {

		val image = FirebaseVisionImage.fromBitmap(bitmap)

		val detector = FirebaseVision.getInstance().visionBarcodeDetector

		detector.detectInImage(image)
			.addOnSuccessListener { firebaseVisionBarcodes ->
				// Task completed successfully
				if (firebaseVisionBarcodes.isNotEmpty()) {
					val resultBitmap = BarcodeBitmapProcessor().drawBoundingBoxes(bitmap, firebaseVisionBarcodes)
					val resultText = firebaseVisionBarcodes.joinToString(",") { it.displayValue.toString() }
					_result.value = Result.Success(bitmap = resultBitmap, text = resultText)
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_no_barcodes))
				}
			}
			.addOnFailureListener { e ->
				// Task failed with an exception
				Log.e(TAG, "Error detecting faces", e)
				val errorMessage = e.message
				if (errorMessage != null) {
					_result.value = Result.Fail(Error.StringError(errorMessage))
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_unknown))
				}
			}

	}

	private fun doFaceDetection(bitmap: Bitmap) {

		val image = FirebaseVisionImage.fromBitmap(bitmap)

		val options = FirebaseVisionFaceDetectorOptions.Builder()
			.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
			.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
			.setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
			.enableTracking()
			.build()

		val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

		detector.detectInImage(image)
			.addOnSuccessListener { firebaseVisionFaces ->
				// Task completed successfully
				if (firebaseVisionFaces.isNotEmpty()) {
					val bitmapResult = FaceDetectionBitmapProcessor().drawBoundingBoxes(bitmap, firebaseVisionFaces)
					val resultText = firebaseVisionFaces.joinToString("\n") { "${it.trackingId} ${it.smilingProbability}" }
					_result.value = Result.Success(bitmap = bitmapResult, text = resultText)
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_no_faces))
				}
			}
			.addOnFailureListener { e ->
				// Task failed with an exception
				Log.e(TAG, "Error detecting faces", e)
				val errorMessage = e.message
				if (errorMessage != null) {
					_result.value = Result.Fail(Error.StringError(errorMessage))
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_unknown))
				}
			}

	}

	private fun doLabelDetection(bitmap: Bitmap) {

		val image = FirebaseVisionImage.fromBitmap(bitmap)
		val detector = FirebaseVision.getInstance().onDeviceImageLabeler

		detector.processImage(image)
			.addOnSuccessListener { firebaseVisionLabels ->
				// Task completed successfully
				if (firebaseVisionLabels.isNotEmpty()) {
					val resultText = firebaseVisionLabels.joinToString(separator = "\n") { "${it.text} : ${it.confidence}" }
					_result.value = Result.Success(text = resultText)
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_no_text))
				}
			}
			.addOnFailureListener { e ->
				// Task failed with an exception
				Log.e(TAG, "Error detecting labels", e)
				val errorMessage = e.message
				if (errorMessage != null) {
					_result.value = Result.Fail(Error.StringError(errorMessage))
				} else {
					_result.value = Result.Fail(Error.ResError(R.string.error_unknown))
				}
			}
	}

}