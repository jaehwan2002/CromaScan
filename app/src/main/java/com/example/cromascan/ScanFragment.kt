package com.example.cromascan

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ScanFragment : Fragment() {

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val bundle = Bundle().apply {
                putString("imageUri", uri.toString())
            }
            findNavController().navigate(
                R.id.action_scanFragment_to_paletteFragment,
                bundle
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonCamera = view.findViewById<Button>(R.id.button_open_camera)
        val buttonGallery = view.findViewById<Button>(R.id.button_open_gallery)
        val buttonHistory = view.findViewById<Button>(R.id.button_open_history)

        buttonCamera.setOnClickListener {
            findNavController().navigate(R.id.action_scanFragment_to_cameraFragment)
        }

        buttonGallery.setOnClickListener {
            openGallery()
        }

        buttonHistory.setOnClickListener {
            findNavController().navigate(R.id.action_scanFragment_to_historyFragment)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}
