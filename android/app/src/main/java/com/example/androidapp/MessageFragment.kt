package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapp.utils.MQTTService
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MessageFragment : Fragment(R.layout.fragment_message) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<TextInputEditText>(R.id.messageEditText)
        val sendBtn = view.findViewById<Button>(R.id.sendBtn)

        sendBtn.setOnClickListener {
            val reportJson = JSONObject().apply {
                put("type", "message")
                put("message", "Message sent")
            }.toString()

            val reportIntent = Intent(requireContext(), MQTTService::class.java).apply {
                action = "PUBLISH"
                putExtra("topic", "device/messageFrag/command/message")
                putExtra("payload", reportJson)
            }
            requireContext().startService(reportIntent)

            findNavController().popBackStack()
        }
    }
}
