package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.androidapp.databinding.FragmentCameraBinding
import com.example.androidapp.databinding.FragmentSensorListBinding
import com.example.androidapp.model.Accident
import com.example.androidapp.model.Vehicle


class SensorListFragment : Fragment() {
    private var _binding : FragmentSensorListBinding? = null
    private val binding get() = _binding!!


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

        val testVehicles = mutableListOf(
            Vehicle(
                id = "V-001",
                location = "Maribor",
                type = "Ambulance",
                acceleration = 2.4
            ),
            Vehicle(
                id = "V-002",
                location = "Ljubljana",
                type = "Police",
                acceleration = 3.1
            )
        )

        val testAccidents = mutableListOf(
            Accident(
                id = "A-100",
                location = "Celje",
                type = "Car crash"
            ),
            Accident(
                id = "A-101",
                location = "Ptuj",
                type = "Fire"
            )
        )

        val vehicleAdapter = VehicleAdapter(testVehicles) { vehicle ->
            println("Clicked vehicle: ${vehicle.id}")
        }

        binding.rvSosVehicles.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = vehicleAdapter
        }

        binding.fabAddVehicle.setOnClickListener {
            findNavController()
                .navigate(R.id.action_sensorListFragment_to_policeFormFragment)
        }

        val accidentAdapter = AccidentAdapter(testAccidents) { accident ->
            println("Clicked accident: ${accident.id}")
        }

        binding.rvAccidents.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = accidentAdapter
        }

    }



}