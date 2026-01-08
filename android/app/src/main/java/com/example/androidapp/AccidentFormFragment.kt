package com.example.androidapp

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidapp.dao.AccidentDao
import com.example.androidapp.dao.LocationDao
import com.example.androidapp.databinding.FragmentAccidentFormBinding
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AccidentFormFragment : Fragment() {

    private var _binding: FragmentAccidentFormBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val gson = Gson()
    //private val SERVER_URL = BuildConfig.SERVER_URL
    private val SERVER_URL = "http://10.0.2.2:3002"
    private var editingAccidentId: String? = null


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

        editingAccidentId = arguments?.getString("accident_id")

        editingAccidentId?.let{ accidentId ->
            loadAccidentForEdit(accidentId)
        }


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

            if (editingAccidentId == null) {
                saveAccident(accidentDao)
            }else{
                editAccident(accidentDao)
            }

            findNavController().navigate(R.id.action_accidentFormFragment_to_sensorListFragment)
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

                binding.tvLocation.text = "Location: ${latitude}, ${longitude}"

                reverseGeocodeOSM(latitude!!, longitude!!) { address ->
                    locationString = address
                    isGeocodingDone = true
                    binding.tvLocation.text = "Location: $address"
                }



            }
        } catch (e: SecurityException) {
            binding.tvLocation.text = "Location: permission missing"
        }
    }

    private fun reverseGeocodeOSM(
        lat: Double,
        lon: Double,
        onResult: (String) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/reverse" +
                            "?format=json&lat=$lat&lon=$lon"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "SOS-Navigator/1.0 (Android)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code}")
                    }

                    val json = JSONObject(response.body!!.string())
                    val address = json.optString("display_name", "$lat, $lon")

                    withContext(Dispatchers.Main) {
                        onResult(address)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("$lat, $lon")
                }
            }
        }
    }


    private fun saveAccident(accident: AccidentDao) {

        val json = JSONObject().apply {
            put("type", accident.typeOfAccident)
            put("latitude", latitude)
            put("longitude", longitude)
            put("locationFreq", accident.locationFreq)
        }


        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$SERVER_URL/api/accident/create")
            .post(body)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val responseBody = response.body?.string()
                println("ACCIDENT CREATED: $responseBody")


            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to save accident",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }


    fun editAccident(
        accident : AccidentDao
    ) {

        val json = JSONObject().apply {
            put("type", accident.typeOfAccident)
            put("latitude", latitude)
            put("longitude", longitude)
            put("locationFreq", accident.locationFreq)
        }


        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$SERVER_URL/api/accident/update/$editingAccidentId")
            .put(body)
            .build()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val responseBody = response.body?.string()
                Log.d("ACCIDENT", "UPDATED: $responseBody")

            } catch (e: Exception) {
                Log.e("ACCIDENT", "Update failed", e)
            }
        }
    }

    private fun loadAccidentForEdit(accidentId: String) {

        getAccidentById(accidentId) { accident ->

            val coords = accident.location.coordinates
            latitude = coords[1]
            longitude = coords[0]

            reverseGeocodeOSM(latitude!!, longitude!!) { address ->

                locationString = address
                isGeocodingDone = true

                binding.tvLocation.text = "Location: $address"
                binding.etLocationFreq.setText(accident.locationFreq.toString())

                val pos = (binding.spinnerType.adapter as ArrayAdapter<String>)
                    .getPosition(accident.typeOfAccident)
                binding.spinnerType.setSelection(pos)
            }
        }
    }




    fun getAccidentById(
        accidentId: String,
        onResult: (AccidentDao) -> Unit
    ) {

        val request = Request.Builder()
            .url("$SERVER_URL/api/accident/$accidentId")
            .get()
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val json = JSONObject(response.body!!.string())

                val loc = json.getJSONObject("location")
                val coords = loc.getJSONArray("coordinates")

                val accident = AccidentDao(
                    id = json.getString("id"),
                    typeOfAccident = json.getString("typeOfAccident"),
                    locationFreq = json.getInt("locationFreq"),
                    location = LocationDao(
                        id = loc.getString("id"),
                        coordinates = listOf(
                            coords.getDouble(0),
                            coords.getDouble(1)
                        )
                    )
                )

                withContext(Dispatchers.Main) {
                    onResult(accident)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}