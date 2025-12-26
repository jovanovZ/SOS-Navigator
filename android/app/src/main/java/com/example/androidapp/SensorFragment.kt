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
import androidx.navigation.fragment.findNavController
import com.example.androidapp.databinding.FragmentMainBinding
import com.example.androidapp.databinding.FragmentSensorBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SensorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SensorFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var accidentSensor = true

    private var _binding: FragmentSensorBinding? = null

    // struktura lista po elemenith [locDay, locHour, locMin, accDay, accHour, accMin]
    private lateinit var numberPickers: List<NumberPicker>

    private val binding get() = _binding!!
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
        numberPickers[4].minValue = 1
        numberPickers[5].minValue = 1
        numberPickers[4].maxValue = 59
        numberPickers[5].maxValue = 59

        numberPickers.forEach { picker ->
            picker.wrapSelectorWheel = true
            picker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
    }

    private fun onSave() {
        val wheelDay = binding.wheelDay.value
        val wheelDay2 = binding.wheelDay2.value

        val wheelHours = binding.wheelHours.value
        val wheelHours2 = binding.wheelHours2.value

        val wheelMinutes = binding.wheelMinutes.value
        val wheelMinutes2 = binding.wheelMinutes2.value

        Log.d(
            "SAVE",
            "dayLoc=$wheelDay dayAcc=$wheelDay2 | hourLoc=$wheelHours hourAcc=$wheelHours2 | minLoc=$wheelMinutes minAcc=$wheelMinutes2"
        )
        findNavController().navigate(R.id.action_sensorFragment_to_mainFragment)

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SensorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SensorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}