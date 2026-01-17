package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.androidapp.dao.AccidentDao
import com.example.androidapp.dao.LocationDao
import com.example.androidapp.dao.VehicleDao
import com.example.androidapp.databinding.FragmentCameraBinding
import com.example.androidapp.databinding.FragmentSensorListBinding
import com.example.androidapp.model.Accident
import com.example.androidapp.model.Vehicle
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class SensorListFragment : Fragment() {
    private var _binding : FragmentSensorListBinding? = null
    private val binding get() = _binding!!

    private lateinit var accidentAdapter: AccidentAdapter
    private val accidents = mutableListOf<Accident>()

    private val vehicles = mutableListOf<Vehicle>()
    private lateinit var vehicleAdapter: VehicleAdapter


    private val client = OkHttpClient()

    private val SERVER_URL = BuildConfig.SERVER_URL

    private val gson = Gson();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("accident_result") { _, bundle ->
            val newAcc = bundle.getParcelable<Accident>("new_accident")
            if (newAcc != null) {
                accidentAdapter.addAccident(newAcc)
                binding.rvAccidents.scrollToPosition(0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        vehicleAdapter = VehicleAdapter(vehicles) { vehicle ->
            findNavController().navigate(
                R.id.action_sensorListFragment_to_policeFormFragment,
                Bundle().apply {
                    putString("vehicle_id", vehicle.id)
                }
            )
        }

        binding.rvSosVehicles.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = vehicleAdapter
        }

        binding.fabAddVehicle.setOnClickListener {
            findNavController()
                .navigate(R.id.action_sensorListFragment_to_policeFormFragment)
        }

       
        accidentAdapter = AccidentAdapter(accidents) { accident ->
            findNavController().navigate(
                R.id.action_sensorListFragment_to_accidentFormFragment,
                Bundle().apply {
                    putString("accident_id", accident.id)
                }
            )
        }


        binding.rvAccidents.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = accidentAdapter
        }

        binding.fabAddAccident.setOnClickListener {
            findNavController().navigate(R.id.action_sensorListFragment_to_accidentFormFragment)
        }

        loadVehiclesFromBackend()
        loadAccidentsFromBackend()
        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_sensorListFragment_to_mainFragment)
        }

    }


    fun getAllVehicles(onResult: (List<VehicleDao>) -> Unit) {

        val request = Request.Builder()
            .url("$SERVER_URL/api/vehicle/all")
            .get()
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val jsonArray = JSONArray(response.body!!.string())
                val vehicles = mutableListOf<VehicleDao>()

                for (i in 0 until jsonArray.length()) {
                    val v = jsonArray.getJSONObject(i)

                    val start = v.getJSONObject("locationStart")
                    val end = v.getJSONObject("locationEnd")

                    vehicles.add(
                        VehicleDao(
                            id = v.getString("id"),
                            type = v.getString("type"),
                            acceleration = v.getDouble("acceleration"),
                            locationFreq = v.getInt("locationFreq"),
                            accelerationFreq = v.getInt("accelerationFreq"),
                            locationStart = LocationDao(
                                id = start.getString("id"),
                                coordinates = listOf(
                                    start.getJSONArray("coordinates").getDouble(0),
                                    start.getJSONArray("coordinates").getDouble(1)
                                )
                            ),
                            locationEnd = LocationDao(
                                id = end.getString("id"),
                                coordinates = listOf(
                                    end.getJSONArray("coordinates").getDouble(0),
                                    end.getJSONArray("coordinates").getDouble(1)
                                )
                            ),
                            timeStamp = ""
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    onResult(vehicles)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllAccidents(onResult: (List<AccidentDao>) -> Unit) {

        val request = Request.Builder()
            .url("$SERVER_URL/api/accident/all")
            .get()
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }

                val jsonArray = JSONArray(response.body!!.string())
                val accidents = mutableListOf<AccidentDao>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val loc = obj.getJSONObject("location")
                    val coords = loc.getJSONArray("coordinates")

                    accidents.add(
                        AccidentDao(
                            id = obj.getString("id"),
                            typeOfAccident = obj.getString("typeOfAccident"),
                            locationFreq = obj.getInt("locationFreq"),
                            location = LocationDao(
                                id = loc.getString("id"),
                                coordinates = listOf(
                                    coords.getDouble(0), // lon
                                    coords.getDouble(1)  // lat
                                )
                            )
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    onResult(accidents)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    private fun loadVehiclesFromBackend() {

        getAllVehicles { vehicleDaos ->

            vehicles.clear()
            vehicleAdapter.notifyDataSetChanged()

            vehicleDaos.forEach { dao ->

                val lat = dao.locationStart.coordinates[1]
                val lon = dao.locationStart.coordinates[0]

                reverseGeocodeOSM(lat, lon) { address ->

                    vehicles.add(
                        Vehicle(
                            id = dao.id,
                            location = address,
                            type = dao.type,
                            acceleration = dao.acceleration
                        )
                    )

                    vehicleAdapter.notifyItemInserted(vehicles.size - 1)
                }
            }
        }
    }

    private fun loadAccidentsFromBackend() {

        getAllAccidents { accidentDaos ->

            accidents.clear()
            accidentAdapter.notifyDataSetChanged()


            accidentDaos.forEach { dao ->
                val lat = dao.location.coordinates[1]
                val lon = dao.location.coordinates[0]

                reverseGeocodeOSM(lat, lon) { address ->
                    accidents.add(
                        Accident(
                            id = dao.id,
                            type = dao.typeOfAccident,
                            location = address
                        )
                    )
                    accidentAdapter.notifyItemInserted(accidents.size-1)
                }
            }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}