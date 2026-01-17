package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapp.databinding.FragmentMessageBinding
import com.example.androidapp.utils.MQTTService
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MessageFragment : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<TextInputEditText>(R.id.messageEditText)

        binding.sendBtn.setOnClickListener {
            val messageText = input.text?.toString() ?: ""

            val reportJson = JSONObject().apply {
                put("type", "message")
                put("message", messageText)
            }.toString()

            val reportIntent = Intent(requireContext(), MQTTService::class.java).apply {
                action = "PUBLISH"
                putExtra("topic", "device/messageFrag/command/message")
                putExtra("payload", reportJson)
            }
            requireContext().startService(reportIntent)

            findNavController().navigate(R.id.action_messageFragment_to_mainFragment)
        }
        binding.button.setOnClickListener {
            findNavController().navigate(R.id.action_messageFragment_to_mainFragment)
        }
    }
}
