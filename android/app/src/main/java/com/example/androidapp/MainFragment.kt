package com.example.androidapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.androidapp.databinding.FragmentMainBinding
import org.osmdroid.config.Configuration

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentMainBinding? = null
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
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences(
                "osmdroid",
                Context.MODE_PRIVATE
            )
        )

        binding.generateBtn.setOnClickListener(this)
        binding.sensorBtn.setOnClickListener(this)
        binding.listBtn.setOnClickListener(this)
        binding.exitBtn.setOnClickListener(this)
        binding.cameraBtn.setOnClickListener(this)
        binding.messageBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.sensorBtn -> findNavController().navigate(R.id.action_mainFragment_to_sensorFragment)
                R.id.listBtn -> findNavController().navigate((R.id.action_mainFragment_to_sensorListFragment))
                R.id.cameraBtn -> findNavController().navigate(R.id.action_mainFragment_to_cameraFragment)
                R.id.messageBtn -> findNavController().navigate(R.id.action_mainFragment_to_messageFragment)
                R.id.exitBtn -> requireActivity().finish()
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}