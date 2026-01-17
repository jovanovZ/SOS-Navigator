package com.example.androidapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.Visibility
import com.example.androidapp.BuildConfig.PYTHON_SERVER_URL
import com.example.androidapp.databinding.FragmentImagePreviewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ImagePreviewFragment : Fragment(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!
    private var imagePath: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private var analyzed = false;

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                fetchLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Lokacija ni dovoljena.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        imagePath = arguments?.getString("imagePath")
        binding.backBtn.setOnClickListener(this)
        binding.analyzeBtn.setOnClickListener(this)
        showImage(imagePath)

        checkLocationAndFetch()
    }


    private fun showImage(imagePath: String?) {
        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)

            val matrix = Matrix().apply {
                postRotate(90f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

            binding.takenImg.setImageBitmap(rotatedBitmap)
            binding.takenImg.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backBtn -> {
                if (!analyzed) {
                    findNavController().navigate(R.id.action_imagePreviewFragment_to_cameraFragment)
                }else{
                    findNavController().navigate(R.id.action_imagePreviewFragment_to_mainFragment)
                }
            }

            R.id.analyzeBtn -> analyzeImage()
        }
    }

    private fun analyzeImage() {
        lifecycleScope.launch {
            showLoading(true)

            try {
                withContext(Dispatchers.IO) {
                    if (userLocation == null || imagePath == null) return@withContext


                    val file = java.io.File(imagePath)

                    val imageMediaType = "image/jpeg".toMediaType()
                    val imageBody = file.asRequestBody(imageMediaType)


                    val multipartBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)

                        .addFormDataPart(
                            name = "lat",
                            value = userLocation!!.latitude.toString()
                        )
                        .addFormDataPart(
                            name = "lon",
                            value = userLocation!!.longitude.toString()
                        )
                        .addFormDataPart(
                            name = "image",
                            filename = file.name,
                            body = imageBody
                        )
                        .build()

                    val request = Request.Builder()
                        .url("$PYTHON_SERVER_URL/analyze")
                        .post(multipartBody)
                        .build()
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build()


                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("Upload failed: $response")
                        }
                        val bodyStr = response.body?.string() ?: ""
                        Log.d("UPLOAD", bodyStr)

                        val json = JSONObject(bodyStr)

                        val vehicleCount = json.optInt("vehicle_count", 0)
                        val trafficStatus = json.optString("traffic_status", "unknown")
                        val base64Img = json.optString("image_base64")
                        var bitmap = base64toBitmap(base64Img)


                        val trafficText = getString(R.string.traffic_status_text, trafficStatus)
                        val carsText = getString(R.string.num_of_cars_text, vehicleCount)
                        analyzed = true
                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                val matrix = Matrix().apply {
                                    postRotate(90f)
                                }
                                bitmap = Bitmap.createBitmap(
                                    bitmap,
                                    0,
                                    0,
                                    bitmap.width,
                                    bitmap.height,
                                    matrix,
                                    true
                                )
                            }
                            binding.takenImg.setImageBitmap(bitmap)
                            binding.trafficStatus.text = trafficText
                            binding.numberOfCars.text = carsText

                            binding.takenImg.visibility =
                                if (bitmap != null) View.VISIBLE else View.GONE
                            binding.trafficStatus.visibility = View.VISIBLE
                            binding.numberOfCars.visibility = View.VISIBLE

                            binding.backBtn.text = "Home"
                            binding.analyzeBtn.visibility = View.GONE
                        }

                    }
                }
            } catch (e: IOException) {
                Log.e("SERVER ERR", e.toString())
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.progressText.visibility = if (show) View.VISIBLE else View.GONE
        binding.takenImg.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(LocationManager::class.java)
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun checkLocationAndFetch() {
        binding.analyzeBtn.isEnabled = false

        if (!isLocationEnabled()) {
            Toast.makeText(
                requireContext(),
                "Lokacija ni vklopljena. Prosimo omogočite GPS.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                    binding.analyzeBtn.isEnabled = true
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Lokacije ni bilo mogoče pridobiti.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.analyzeBtn.isEnabled = false
                }
            }
            .addOnFailureListener {
                binding.analyzeBtn.isEnabled = false
            }
    }

    fun base64toBitmap(base64String: String): Bitmap? {
        return try {
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substring(base64String.indexOf(",") + 1)
            } else {
                base64String
            }
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}