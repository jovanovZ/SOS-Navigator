package com.example.androidapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlin.random.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidapp.dao.LocationDao
import com.example.androidapp.dao.VehicleDao
import com.example.androidapp.databinding.FragmentPoliceFormBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL



class PoliceFormFragment : Fragment() {

    private var _binding: FragmentPoliceFormBinding? = null
    private val binding get() = _binding!!

    private var editingPoliceVehicleId: String? = null

    private lateinit var map: MapView

    private  var startMarker : Marker? = null

    private  var endMarker : Marker? = null


    private val client = OkHttpClient()

    private val SERVER_URL = "http://10.0.2.2:3002"
    //private val SERVER_URL = BuildConfig.SERVER_URL

    private val gson = Gson();


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoliceFormBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingPoliceVehicleId = arguments?.getString("vehicle_id")

        map = binding.mapPickerPolice
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(46.558927, 15.637981))

        editingPoliceVehicleId?.let { vehicleId ->
            loadVehicleForEdit(vehicleId)
        }


        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p ?: return false

                if (startMarker == null){
                    reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                        binding.etPoliceFormStartLocation.setText(address)
                        binding.etPoliceFormStartLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormStartLongitude.setText(p.longitude.toString())
                    }

                    val point = GeoPoint(p.latitude, p.longitude)

                    startMarker = Marker(map)
                    startMarker?.position = point
                    startMarker?.title = "Start Position"
                    startMarker?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )
                    map.overlays.add(startMarker)

                    return true;

                }else if(endMarker == null){
                    reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                        binding.etPoliceFormEndLocation.setText(address)
                        binding.etPoliceFormEndLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormEndLongitude.setText(p.longitude.toString())
                    }

                    val point = GeoPoint(p.latitude, p.longitude)

                    endMarker = Marker(map)
                    endMarker?.position = point
                    endMarker?.title = "End Position"
                    endMarker?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    map.overlays.add(endMarker)

                    return true
                }


                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                TODO("Not yet implemented")
            }

        }))

        binding.btnSavePoliceVehicle.setOnClickListener {

            val latStart = binding.etPoliceFormStartLatitude.text.toString().toDouble()
            val lonStart = binding.etPoliceFormStartLongitude.text.toString().toDouble()
            val latEnd = binding.etPoliceFormEndLatitude.text.toString().toDouble()
            val lonEnd = binding.etPoliceFormEndLongitude.text.toString().toDouble()
            val accelStart = binding.etPoliceFormRangeStartAcceleration.text.toString().toDouble()
            val accelEnd = binding.etPoliceFormRangeEndAcceleration.text.toString().toDouble()
            val randomAcceleration = Random.nextDouble(accelStart, accelEnd)



            if (editingPoliceVehicleId == null) {
                createVehicle(latStart, lonStart, latEnd, lonEnd,randomAcceleration)
            } else {
                editVehicle(latStart, lonStart, latEnd, lonEnd,randomAcceleration)
            }

            findNavController().navigate(R.id.action_policeFormFragment_to_sensorListFragment)
        }




    }

    fun reverseGeocodeOSM(
        lat: Double,
        lon: Double,
        onResult: (String) -> Unit
    ) {
        Thread {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/reverse" +
                            "?format=json&lat=$lat&lon=$lon"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "ProektApp")

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val address = json.getString("display_name")

                requireActivity().runOnUiThread {
                    onResult(address)
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    onResult("Unknown location")
                }
            }
        }.start()
    }


    fun createVehicle(
        latStart: Double,
        lonStart: Double,
        latEnd: Double,
        lonEnd: Double,
        accel: Double

    ) {

        val json = JSONObject().apply {
            put("latStart", latStart)
            put("latEnd", latEnd)
            put("longStart", lonStart)
            put("longEnd", lonEnd)
            put("type", "Police")
            put("acceleration", accel)
            put("locationFreq", 1440)
            put("accelerationFreq", 1440)
        }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$SERVER_URL/api/vehicle/create")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("CREATED: $responseBody")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }






    fun editVehicle(
        latStart: Double,
        lonStart: Double,
        latEnd: Double,
        lonEnd: Double,
        accel: Double
    ) {

        val json = JSONObject().apply {
            put("latStart", latStart)
            put("longStart", lonStart)
            put("latEnd", latEnd)
            put("longEnd", lonEnd)
            put("type", "Police")
            put("acceleration", accel)
            put("locationFreq", 1440)
            put("accelerationFreq", 1440)
        }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$SERVER_URL/api/vehicle/update/$editingPoliceVehicleId")
            .put(body)
            .build()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val responseBody = response.body?.string()
                Log.d("VEHICLE", "UPDATED: $responseBody")

            } catch (e: Exception) {
                Log.e("VEHICLE", "Update failed", e)
            }
        }
    }


    fun getVehicleById(
        vehicleId: String,
        onResult: (VehicleDao) -> Unit
    ) {

        val request = Request.Builder()
            .url("$SERVER_URL/api/vehicle/$vehicleId")
            .get()
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val json = JSONObject(response.body!!.string())

                val startJson = json.getJSONObject("locationStart")
                val endJson = json.getJSONObject("locationEnd")

                val vehicle = VehicleDao(
                    id = json.getString("id"),
                    type = json.getString("type"),
                    acceleration = json.getDouble("acceleration"),
                    locationFreq = json.getInt("locationFreq"),
                    accelerationFreq = json.getInt("accelerationFreq"),
                    locationStart = LocationDao(
                        id = startJson.getString("id"),
                        coordinates = listOf(
                            startJson.getJSONArray("coordinates").getDouble(0),
                            startJson.getJSONArray("coordinates").getDouble(1)
                        )
                    ),
                    locationEnd = LocationDao(
                        id = endJson.getString("id"),
                        coordinates = listOf(
                            endJson.getJSONArray("coordinates").getDouble(0),
                            endJson.getJSONArray("coordinates").getDouble(1)
                        )
                    ),
                    timeStamp = ""
                )
                Log.e("READ"," $vehicle")

                withContext(Dispatchers.Main) {
                    onResult(vehicle)
                }



            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun loadVehicleForEdit(vehicleId: String) {

        getVehicleById(vehicleId) { vehicle ->

            // START
            val startCoords = vehicle.locationStart.coordinates
            val startPoint = GeoPoint(
                startCoords[1], // lat
                startCoords[0]  // lon
            )

            /*binding.etPoliceFormStartLatitude.setText(startPoint.latitude.toString())
            binding.etPoliceFormStartLongitude.setText(startPoint.longitude.toString())*/

            reverseGeocodeOSM(startPoint.latitude, startPoint.longitude) { address ->
                binding.etPoliceFormStartLocation.setText(address)
                binding.etPoliceFormStartLatitude.setText(startPoint.latitude.toString())
                binding.etPoliceFormStartLongitude.setText(startPoint.longitude.toString())
            }



            startMarker?.let { map.overlays.remove(it) }
            startMarker = Marker(map).apply {
                position = startPoint
                title = "Start"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                isDraggable = true

                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker?) {}

                    override fun onMarkerDrag(marker: Marker?) {}

                    override fun onMarkerDragEnd(marker: Marker?) {
                        marker ?: return
                        val p = marker.position

                        binding.etPoliceFormStartLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormStartLongitude.setText(p.longitude.toString())

                        reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                            binding.etPoliceFormStartLocation.setText(address)
                        }
                    }
                })

            }
            map.overlays.add(startMarker)

            // END
            val endCoords = vehicle.locationEnd.coordinates
            val endPoint = GeoPoint(
                endCoords[1],
                endCoords[0]
            )


            /*binding.etPoliceFormEndLatitude.setText(endPoint.latitude.toString())
            binding.etPoliceFormEndLongitude.setText(endPoint.longitude.toString())*/

            reverseGeocodeOSM(endPoint.latitude, endPoint.longitude) { address ->
                binding.etPoliceFormEndLocation.setText(address)
                binding.etPoliceFormEndLatitude.setText(endPoint.latitude.toString())
                binding.etPoliceFormEndLongitude.setText(endPoint.longitude.toString())
            }

            endMarker?.let { map.overlays.remove(it) }
            endMarker = Marker(map).apply {
                position = endPoint
                title = "End"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


                isDraggable = true

                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker?) {}

                    override fun onMarkerDrag(marker: Marker?) {}

                    override fun onMarkerDragEnd(marker: Marker?) {
                        marker ?: return
                        val p = marker.position

                        binding.etPoliceFormStartLatitude.setText(p.latitude.toString())
                        binding.etPoliceFormStartLongitude.setText(p.longitude.toString())

                        reverseGeocodeOSM(p.latitude, p.longitude) { address ->
                            binding.etPoliceFormStartLocation.setText(address)
                        }
                    }
                })
            }
            map.overlays.add(endMarker)

            map.invalidate()
        }
    }






    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}