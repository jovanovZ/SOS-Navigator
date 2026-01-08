package com.example.androidapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidapp.databinding.FragmentSensorBinding
import com.example.androidapp.dao.AccidentResponse
import com.example.androidapp.model.RunTimeSensor
import com.example.androidapp.model.SensorType
import com.example.androidapp.dao.VehicleResponse
import com.example.androidapp.utils.Util
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.random.Random

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SensorFragment : Fragment(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var accidentSensor = true

    private var _binding: FragmentSensorBinding? = null

    // struktura lista po elemenith [locDay, locHour, locMin, accDay, accHour, accMin]
    private lateinit var numberPickers: List<NumberPicker>

    private val binding get() = _binding!!
    private val client = OkHttpClient()
    //private val SERVER_URL = BuildConfig.SERVER_URL
    private val SERVER_URL = "http://10.0.2.2:3002"

    private val gson = Gson()


    private val runTimeViewModel: SensorRunTimeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textViewSensor.setOnClickListener(this)
        binding.sensorFragBack.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        configureNumberPickers()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewSensor -> showPopupMenu(v)
            R.id.sensorFragBack -> findNavController().navigate(R.id.action_sensorFragment_to_mainFragment)
            R.id.btnSave -> onSave()
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            binding.textViewSensor.text = "${menuItem.title} sensor"
            accidentSensor = menuItem.title == "Accident"
            updateUI()
            true
        }
        popupMenu.show()
    }

    private fun updateUI() {
        if (accidentSensor) {
            binding.wheelDay2.visibility = View.GONE
            binding.wheelMinutes2.visibility = View.GONE
            binding.wheelHours2.visibility = View.GONE
            binding.textView5.visibility = View.GONE
            binding.textView3.visibility = View.GONE
        } else {
            binding.wheelDay2.visibility = View.VISIBLE
            binding.wheelMinutes2.visibility = View.VISIBLE
            binding.wheelHours2.visibility = View.VISIBLE
            binding.textView5.visibility = View.VISIBLE
            binding.textView3.visibility = View.VISIBLE
        }
    }

    private fun configureNumberPickers() {
        numberPickers = listOf(
            binding.wheelDay,
            binding.wheelDay2,
            binding.wheelHours,
            binding.wheelHours2,
            binding.wheelMinutes,
            binding.wheelMinutes2,
        )
        // denve
        numberPickers[0].minValue = 0
        numberPickers[1].minValue = 0
        numberPickers[0].maxValue = 13
        numberPickers[1].maxValue = 13

        // ure
        numberPickers[2].minValue = 0
        numberPickers[3].minValue = 0
        numberPickers[2].maxValue = 23
        numberPickers[3].maxValue = 23

        // minute
        numberPickers[4].minValue = 0
        numberPickers[5].minValue = 0
        numberPickers[4].maxValue = 59
        numberPickers[5].maxValue = 59

        numberPickers.forEach { picker ->
            picker.wrapSelectorWheel = true
            picker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
    }

    private fun onSave() {
        val numberOfGenerations = validateInputsAndReturnNumberOfGenerations()

        if (accidentSensor) {
            generateAccidents(numberOfGenerations)
        } else {
            generatePoliceCars(numberOfGenerations)
        }
        findNavController().navigate(R.id.action_sensorFragment_to_mainFragment)

    }


    private fun validateInputsAndReturnNumberOfGenerations(): Int {
        val wheelDay = binding.wheelDay.value
        val wheelDay2 = binding.wheelDay2.value

        val wheelHours = binding.wheelHours.value
        val wheelHours2 = binding.wheelHours2.value

        val wheelMinutes = binding.wheelMinutes.value
        val wheelMinutes2 = binding.wheelMinutes2.value
        val numberOfGenerationsText = binding.editTextNumber.text.toString()
        val numberOfGenerations = numberOfGenerationsText.toIntOrNull()
        if (numberOfGenerations == 0 || numberOfGenerations == null) {
            Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
            return 0

        } else if (accidentSensor) {
            if (wheelDay == 0 && wheelMinutes == 0 && wheelHours == 0) {
                Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                return 0
            }
        } else {
            if ((wheelDay == 0 && wheelMinutes == 0 && wheelHours == 0) || (wheelDay2 == 0 && wheelMinutes2 == 0 && wheelHours2 == 0)) {
                Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                return 0
            }
        }

        return numberOfGenerations
    }

    private fun generateAccidents(n: Int) {
        val wheelDay = binding.wheelDay.value
        val wheelHours = binding.wheelHours.value
        val wheelMinutes = binding.wheelMinutes.value
        val types = listOf("prometna", "naravna nesreča", "zdravstveni primer", "kriminal")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                for (i in 1..n) {
                    val randomIndex = Random.nextInt(types.size)
                    val longLat: Pair<Double, Double> = Util.getRandomSloveniaLatLng()
                    val freq = Util.convertToMin(wheelDay, wheelHours, wheelMinutes)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val jsonBody = """
                        {
                            "latitude": ${longLat.first},
                            "longitude": ${longLat.second},
                            "type": "${types[randomIndex]}",
                            "locationFreq": "$freq"
                        }
                    """.trimIndent()
                    val requestBody = jsonBody.toRequestBody(mediaType)

                    val req =
                        Request.Builder()
                            .post(requestBody)
                            .url("$SERVER_URL/api/accident/create")
                            .build()

                    client.newCall(req).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Response error $response")
                        val responseData = response.body?.string()
                        val responseObj = gson.fromJson(responseData, AccidentResponse::class.java)
                        val accident = responseObj.accident
                        Log.d("SERVER", "Accident created: $accident")
                        runTimeViewModel.addSensor(
                            RunTimeSensor(
                                accident.id,
                                SensorType.ACCIDENT_LOCATION,
                                accident.locationFreq
                            )
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "$n accident sensors generated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SERVER_ERROR", "Failed to create $n accidents", e)
            }
        }
    }

    private fun generatePoliceCars(n: Int) {
        val wheelDay = binding.wheelDay.value
        val wheelDay2 = binding.wheelDay2.value

        val wheelHours = binding.wheelHours.value
        val wheelHours2 = binding.wheelHours2.value

        val wheelMinutes = binding.wheelMinutes.value
        val wheelMinutes2 = binding.wheelMinutes2.value

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                for (i in 1..n) {
                    val longLatStart: Pair<Double, Double> = Util.getRandomSloveniaLatLng()
                    val longLatEnd: Pair<Double, Double> = Util.getRandomSloveniaLatLng()
                    val locFreq = Util.convertToMin(wheelDay, wheelHours, wheelMinutes)
                    val accFreq = Util.convertToMin(wheelDay2, wheelHours2, wheelMinutes2)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val jsonBody = """
                        {
                            "latStart": ${longLatStart.first},
                            "longStart": ${longLatStart.second},
                            "latEnd": ${longLatEnd.first},
                            "longEnd": ${longLatEnd.second},
                            "locationFreq": "$locFreq",
                            "accelerationFreq": "$accFreq" 
                        }
                    """.trimIndent()
                    val requestBody = jsonBody.toRequestBody(mediaType)

                    val req =
                        Request.Builder()
                            .post(requestBody)
                            .url("$SERVER_URL/api/vehicle/create")
                            .build()

                    client.newCall(req).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Response error $response")
                        val responseData = response.body?.string()
                        val responseObj = gson.fromJson(responseData, VehicleResponse::class.java)
                        val vehicle = responseObj.vehicle

                        Log.d("SERVER", "Police car created: $vehicle")
                        runTimeViewModel.addSensor(
                            RunTimeSensor(
                                vehicle.id,
                                SensorType.VEHICLE_LOCATION,
                                vehicle.locationFreq
                            )
                        )
                        runTimeViewModel.addSensor(
                            RunTimeSensor(
                                vehicle.id,
                                SensorType.VEHICLE_ACCELERATION,
                                vehicle.accelerationFreq
                            )
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "$n car sensors generated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SERVER_ERROR", "Failed to create $n police car", e)
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = SensorFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}