package com.example.androidapp

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidapp.dao.AccidentDao
import com.example.androidapp.dao.LocationDao
import com.example.androidapp.databinding.FragmentAccidentFormBinding
import com.example.androidapp.model.Accident
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AccidentFormFragment : Fragment() {

    private var _binding: FragmentAccidentFormBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val gson = Gson()
    private val SERVER_URL = BuildConfig.SERVER_URL

    private var latitude: Double? = null
    private var longitude: Double? = null

    private var locationString: String = "Unknown"
    private var isGeocodingDone: Boolean = false

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) fetchLocation()
            else {
                binding.tvLocation.text = "Location: permission denied"
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccidentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = listOf("prometna", "naravna nesreča", "zdravstveni primer", "kriminal")
        binding.spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        binding.btnCancelAccident.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveAccident.setOnClickListener {
            println("DEBUG: Save button clicked")

            val freq = binding.etLocationFreq.text?.toString()?.trim()?.toIntOrNull()
            println("DEBUG: freq = $freq")

            if (freq == null || freq <= 0) {
                Toast.makeText(requireContext(), "Invalid frequency", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lat = latitude
            val lon = longitude
            println("DEBUG: lat = $lat, lon = $lon")

            if (lat == null || lon == null) {
                Toast.makeText(requireContext(), "Location not ready yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            println("DEBUG: isGeocodingDone = $isGeocodingDone")
            if (!isGeocodingDone) {
                Toast.makeText(requireContext(), "Wait for location...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            println("DEBUG: All validations passed, saving...")

            val type = binding.spinnerType.selectedItem.toString()

            val locationDao = LocationDao(
                id = "",
                coordinates = listOf(lon, lat)
            )

            val accidentDao = AccidentDao(
                id = "",
                typeOfAccident = type,
                location = locationDao,
                locationFreq = freq
            )

            saveAccident(accidentDao)
        }
    }

    private fun fetchLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc == null) {
                    binding.tvLocation.text = "Location: unavailable"
                    return@addOnSuccessListener
                }

                latitude = loc.latitude
                longitude = loc.longitude
                binding.tvLocation.text = "Location: ${loc.latitude}, ${loc.longitude}"
                reverseGeocodeOSM(loc.latitude, loc.longitude)
            }
        } catch (e: SecurityException) {
            binding.tvLocation.text = "Location: permission missing"
        }
    }

    private fun reverseGeocodeOSM(lat: Double, lon: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lon&zoom=18&addressdetails=1"

                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "SOS-Navigator/1.0 (android)")
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) throw Exception("Geocode HTTP ${resp.code}")

                    val body = resp.body?.string().orEmpty()
                    val json = gson.fromJson(body, Map::class.java)

                    val displayName = json["display_name"]?.toString()
                    val finalName = displayName?.takeIf { it.isNotBlank() } ?: "$lat, $lon"

                    withContext(Dispatchers.Main) {
                        locationString = finalName
                        isGeocodingDone = true
                        binding.tvLocation.text = "Location: $finalName"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    locationString = "$lat, $lon"
                    isGeocodingDone = true
                    binding.tvLocation.text = "Location: $locationString"
                }
            }
        }
    }

    private fun saveAccident(accident: AccidentDao) {

        val newAccident = Accident(
            id = "A-${System.currentTimeMillis()}",
            location = locationString,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            type = accident.typeOfAccident
        )

        setFragmentResult(
            "accident_result",
            bundleOf("new_accident" to newAccident)
        )

        findNavController().popBackStack()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}