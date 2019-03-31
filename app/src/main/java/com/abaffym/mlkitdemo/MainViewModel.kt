package com.abaffym.mlkitdemo

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abaffym.mlkitdemo.bitmapprocessor.BarcodeBitmapProcessor
import com.abaffym.mlkitdemo.bitmapprocessor.FaceDetectionBitmapProcessor
import com.abaffym.mlkitdemo.bitmapprocessor.LabelDetectionBitmapProcessor
import com.abaffym.mlkitdemo.bitmapprocessor.TextRecognitionBitmapProcessor
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.text.FirebaseVisionText

private const val TAG = "MainViewModel"

sealed class Error {
	data class StringError(val message: String) : Error()
	data class ResError(val errorId: Int) : Error()
}

sealed class Result {
	object Progress : Result()
	data class Success(val bitmap: Bitmap? = null) : Result()
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

		val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

		textRecognizer.processImage(image)
			.addOnSuccessListener { text: FirebaseVisionText ->
				text.textBlocks.forEach { textBlocks ->
					textBlocks.lines.forEach { lines ->
						lines.elements.forEach { element ->
							element.text
							element.confidence
							element.recognizedLanguages
							element.boundingBox
						}
					}
				}
				// Task completed successfully
				if (text.text.isNotEmpty()) {
					val resultBitmap = TextRecognitionBitmapProcessor().draw(bitmap, text)
					_result.value = Result.Success(bitmap = resultBitmap)
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

		val barcodeDetector = FirebaseVision.getInstance().visionBarcodeDetector

		barcodeDetector.detectInImage(image)
			.addOnSuccessListener { barcodes: List<FirebaseVisionBarcode> ->
				// Task completed successfully
				if (barcodes.isNotEmpty()) {
					val resultBitmap = BarcodeBitmapProcessor().drawBoundingBoxes(bitmap, barcodes)
					_result.value = Result.Success(bitmap = resultBitmap)
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
			.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
			.enableTracking()
			.build()

		val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

		faceDetector.detectInImage(image)
			.addOnSuccessListener { faces: List<FirebaseVisionFace> ->
				// Task completed successfully
				if (faces.isNotEmpty()) {
					val bitmapResult = FaceDetectionBitmapProcessor().draw(bitmap, faces)
					_result.value = Result.Success(bitmap = bitmapResult)
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
		val imageLabeler = FirebaseVision.getInstance().onDeviceImageLabeler

		imageLabeler.processImage(image)
			.addOnSuccessListener { labels: List<FirebaseVisionImageLabel> ->
				labels.forEach { label: FirebaseVisionImageLabel ->
					label.text
					label.confidence
					label.entityId
				}
				// Task completed successfully
				if (labels.isNotEmpty()) {
					val bitmapResult = LabelDetectionBitmapProcessor().draw(bitmap, labels)
					_result.value = Result.Success(bitmap = bitmapResult)
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