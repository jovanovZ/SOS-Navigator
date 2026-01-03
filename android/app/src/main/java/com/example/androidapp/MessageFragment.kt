package com.example.androidapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class MessageFragment : Fragment(R.layout.fragment_message) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<TextInputEditText>(R.id.messageEditText)
        val sendBtn = view.findViewById<Button>(R.id.sendBtn)

        sendBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
