package com.example.cromascan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.File
import java.io.FileOutputStream

class MatchFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var viewSelectedColor: View
    private lateinit var textSelectedHex: TextView
    private lateinit var viewMatch1: View
    private lateinit var viewMatch2: View
    private lateinit var viewMatch3: View
    private lateinit var buttonSaveHistory: Button
    private lateinit var buttonShareImage: Button
    private lateinit var buttonBackToStart: Button

    private var baseColor: Int = Color.BLACK
    private var baseHex: String = "#000000"
    private var matchColors: IntArray = intArrayOf(Color.DKGRAY, Color.GRAY, Color.LTGRAY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view

        viewSelectedColor = view.findViewById(R.id.view_selected_color)
        textSelectedHex = view.findViewById(R.id.text_selected_hex)
        viewMatch1 = view.findViewById(R.id.view_match_1)
        viewMatch2 = view.findViewById(R.id.view_match_2)
        viewMatch3 = view.findViewById(R.id.view_match_3)
        buttonSaveHistory = view.findViewById(R.id.button_save_history)
        buttonShareImage = view.findViewById(R.id.button_share_image)
        buttonBackToStart = view.findViewById(R.id.button_back_to_start)

        val args = requireArguments()

        val hasHistoryData =
            args.containsKey("baseColor") &&
                    args.containsKey("baseHex") &&
                    args.containsKey("matchColors")

        if (hasHistoryData) {
            baseColor = args.getInt("baseColor", Color.BLACK)
            baseHex = args.getString("baseHex") ?: String.format("#%06X", 0xFFFFFF and baseColor)
            matchColors = args.getIntArray("matchColors")
                ?: ColorMatchCalculator.calculateMatches(baseColor)
        } else if (args.containsKey("selectedColor")) {
            baseColor = args.getInt("selectedColor", Color.BLACK)
            baseHex = String.format("#%06X", 0xFFFFFF and baseColor)
            matchColors = ColorMatchCalculator.calculateMatches(baseColor)
        } else {
            baseColor = Color.BLACK
            baseHex = "#000000"
            matchColors = ColorMatchCalculator.calculateMatches(baseColor)
        }

        applyColorsToViews()

        viewMatch1.setOnClickListener { onMatchColorClicked(0) }
        viewMatch2.setOnClickListener { onMatchColorClicked(1) }
        viewMatch3.setOnClickListener { onMatchColorClicked(2) }

        buttonSaveHistory.setOnClickListener {
            if (matchColors.size < 3) {
                Toast.makeText(requireContext(), "저장할 색상 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val item = ColorHistoryItem(
                baseHex = baseHex,
                matchColor1 = matchColors[0],
                matchColor2 = matchColors[1],
                matchColor3 = matchColors[2],
                timestamp = System.currentTimeMillis()
            )
            ColorHistoryStorage.saveColorHistory(requireContext(), item)
            Toast.makeText(requireContext(), "히스토리에 저장했습니다.", Toast.LENGTH_SHORT).show()
        }

        buttonShareImage.setOnClickListener {
            shareMatchAsImage()
        }

        buttonBackToStart.setOnClickListener {
            val navController = findNavController()
            val popped = navController.popBackStack(R.id.scanFragment, false)
            if (!popped) {
                navController.navigate(R.id.scanFragment)
            }
        }
    }

    private fun applyColorsToViews() {
        viewSelectedColor.setBackgroundColor(baseColor)
        textSelectedHex.text = baseHex

        if (matchColors.isNotEmpty()) {
            viewMatch1.setBackgroundColor(matchColors[0])
        }
        if (matchColors.size > 1) {
            viewMatch2.setBackgroundColor(matchColors[1])
        }
        if (matchColors.size > 2) {
            viewMatch3.setBackgroundColor(matchColors[2])
        }
    }

    private fun onMatchColorClicked(index: Int) {
        if (index !in matchColors.indices) return

        baseColor = matchColors[index]
        baseHex = String.format("#%06X", 0xFFFFFF and baseColor)
        matchColors = ColorMatchCalculator.calculateMatches(baseColor)
        applyColorsToViews()
    }

    private fun shareMatchAsImage() {
        if (rootView.width == 0 || rootView.height == 0) {
            Toast.makeText(requireContext(), "화면이 아직 그려지지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)

        val cacheDir = requireContext().cacheDir
        val file = File(cacheDir, "match_share.png")

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = try {
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "파일 공유 준비에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "이미지 공유하기"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "공유할 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
