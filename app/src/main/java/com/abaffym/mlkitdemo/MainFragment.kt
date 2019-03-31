package com.abaffym.mlkitdemo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_main.btn_pick_photo
import kotlinx.android.synthetic.main.fragment_main.iv_photo
import kotlinx.android.synthetic.main.fragment_main.progress_container
import kotlinx.android.synthetic.main.fragment_main.spinner_feature
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders

private const val PICK_IMAGE_REQUEST_CODE = 1234

class MainFragment : Fragment() {

	private lateinit var viewModel: MainViewModel

	companion object {
		fun newInstance() = MainFragment()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_main, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		setOnClickListeners()
		setViewModelObservers()
		setupFeatureSpinner()
	}

	private fun setOnClickListeners() {
		btn_pick_photo.setOnClickListener {
			startImagePickerActivity()
		}
	}

	private fun setViewModelObservers() {
		viewModel.bitmap.observe(this, Observer { bitmap ->
			onBitmapChanged(bitmap)
		})
		viewModel.result.observe(this, Observer { result ->
			onResultChanged(result)
		})
	}

	private fun setupFeatureSpinner() {
		val list = features.map { context?.getString(it.second) }
		val spinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list)

		spinner_feature.apply {
			adapter = spinnerAdapter.apply {
				setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
			}
			onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
					viewModel.feature.value = features[position].first
				}

				override fun onNothingSelected(parent: AdapterView<*>) {}

			}
		}
	}

	private fun startImagePickerActivity() {
		val photoGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
			type = "image/*"
		}
		startActivityForResult(photoGalleryIntent, PICK_IMAGE_REQUEST_CODE)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
		super.onActivityResult(requestCode, resultCode, result)
		when (requestCode) {
			PICK_IMAGE_REQUEST_CODE -> {
				if (resultCode == Activity.RESULT_OK) {
					val uri = result?.data ?: return
					val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
					processBitmap(bitmap)
				}
			}
		}
	}

	private fun processBitmap(bitmap: Bitmap) {
		viewModel.bitmap.value = bitmap
		progress_container.visibility = View.VISIBLE
	}

	private fun onBitmapChanged(bitmap: Bitmap) {
		iv_photo.setImageBitmap(bitmap)
	}

	private fun onResultChanged(result: Result) = when (result) {
		is Result.Progress -> {
			progress_container.visibility = View.VISIBLE
			iv_photo.setImageBitmap(null)
		}
		is Result.Success -> {
			progress_container.visibility = View.GONE
			iv_photo.setImageBitmap(result.bitmap ?: viewModel.bitmap.value)
		}
		is Result.Fail -> {
			progress_container.visibility = View.GONE
			iv_photo.setImageBitmap(viewModel.bitmap.value)
			val errorMessage = when (result.error) {
				is Error.StringError -> result.error.message
				is Error.ResError -> getString(result.error.errorId)
			}
			Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

		}
	}

}
