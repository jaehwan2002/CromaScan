package com.example.cromascan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PaletteFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var colorContainer: LinearLayout
    private lateinit var buttonGoMatch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonK3: Button
    private lateinit var buttonK5: Button
    private lateinit var buttonK8: Button

    private lateinit var recommendContainer: LinearLayout
    private lateinit var recommendTitleRow: LinearLayout
    private lateinit var recommendCloseButton: Button
    private lateinit var recommendToggleButton: Button

    private var selectedColor: Int = 0
    private var selectedItemView: View? = null
    private var currentK: Int = 8
    private var currentBitmap: Bitmap? = null

    private var recommendSelectedView: View? = null
    private var recommendSelectedColor: Int? = null

    private var isRecommendOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_palette, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePreview = view.findViewById(R.id.image_preview)
        colorContainer = view.findViewById(R.id.color_container)
        buttonGoMatch = view.findViewById(R.id.button_go_match)
        progressBar = view.findViewById(R.id.progress_loading)

        buttonK3 = view.findViewById(R.id.button_k3)
        buttonK5 = view.findViewById(R.id.button_k5)
        buttonK8 = view.findViewById(R.id.button_k8)

        recommendContainer = view.findViewById(R.id.recommend_palette_container)
        recommendTitleRow = view.findViewById(R.id.recommend_title_row)
        recommendCloseButton = view.findViewById(R.id.button_close_recommend)
        recommendToggleButton = view.findViewById(R.id.button_toggle_recommend)

        // 🔥 처음에는 추천 팔레트 닫혀있음
        recommendContainer.visibility = View.GONE
        recommendTitleRow.visibility = View.GONE
        isRecommendOpen = false

        showLoading(true)

        val imageUriString = arguments?.getString("imageUri")
        if (imageUriString.isNullOrEmpty()) {
            showErrorAndExit("이미지 정보가 없습니다.")
            return
        }

        val imageUri = Uri.parse(imageUriString)
        val bitmap = loadScaledBitmap(imageUri)

        if (bitmap == null) {
            showErrorAndExit("이미지를 불러오지 못했습니다.")
            return
        }

        currentBitmap = bitmap
        imagePreview.setImageBitmap(bitmap)

        updateKButtons()
        extractColors(bitmap)
        createRecommendPalette(bitmap)

        // 매칭 보기 버튼
        buttonGoMatch.setOnClickListener {
            val finalColor = recommendSelectedColor ?: selectedColor
            if (finalColor == 0) {
                Toast.makeText(requireContext(), "먼저 색상을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bundle = Bundle().apply {
                putInt("selectedColor", finalColor)
            }
            findNavController().navigate(R.id.action_paletteFragment_to_matchFragment, bundle)
        }

        buttonK3.setOnClickListener { updateKAndRefresh(3) }
        buttonK5.setOnClickListener { updateKAndRefresh(5) }
        buttonK8.setOnClickListener { updateKAndRefresh(8) }

        // 🔥 추천 팔레트 토글 버튼
        recommendToggleButton.setOnClickListener {
            toggleRecommendPalette()
        }

        // 추천 팔레트 X 버튼
        recommendCloseButton.setOnClickListener {
            closeRecommendPalette()
        }
    }

    private fun toggleRecommendPalette() {
        if (isRecommendOpen) closeRecommendPalette()
        else openRecommendPalette()
    }

    private fun openRecommendPalette() {
        recommendTitleRow.visibility = View.VISIBLE
        recommendContainer.visibility = View.VISIBLE
        recommendToggleButton.text = "추천 팔레트 닫기"
        isRecommendOpen = true
    }

    private fun closeRecommendPalette() {
        recommendTitleRow.visibility = View.GONE
        recommendContainer.visibility = View.GONE
        recommendToggleButton.text = "추천 팔레트 보기"
        isRecommendOpen = false

        recommendSelectedView?.setBackgroundColor(Color.TRANSPARENT)
        recommendSelectedColor = null
        recommendSelectedView = null
    }

    private fun updateKAndRefresh(k: Int) {
        currentK = k
        updateKButtons()
        currentBitmap?.let {
            showLoading(true)
            extractColors(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        imagePreview.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        colorContainer.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    private fun showErrorAndExit(message: String) {
        showLoading(false)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun loadScaledBitmap(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val maxSize = 1080
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
            options.inJustDecodeBounds = false

            requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(opt: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
        val (h, w) = opt.outHeight to opt.outWidth
        var sample = 1
        if (h > reqH || w > reqW) {
            var halfH = h / 2
            var halfW = w / 2
            while ((halfH / sample) >= reqH && (halfW / sample) >= reqW) sample *= 2
        }
        return sample
    }

    private fun extractColors(bitmap: Bitmap) {
        val colors = KMeansColorExtractor.extractDominantColors(bitmap, currentK)
        if (colors.isEmpty()) {
            showErrorAndExit("이미지에서 색상을 추출하지 못했습니다.")
            return
        }

        colorContainer.removeAllViews()
        selectedItemView = null
        selectedColor = 0

        colors.forEachIndexed { index, colorInt ->
            val item = createColorItem(colorInt, isRecommend = false)
            colorContainer.addView(item)

            if (index == 0) {
                item.setBackgroundColor(Color.argb(40, 108, 75, 217))
                selectedItemView = item
                selectedColor = colorInt
            }
        }

        showLoading(false)
    }

    private fun createRecommendPalette(bitmap: Bitmap) {
        val colors = KMeansColorExtractor.extractDominantColors(bitmap, 5)
        if (colors.isEmpty()) return

        recommendContainer.removeAllViews()

        colors.take(3).forEach { colorInt ->
            recommendContainer.addView(createColorItem(colorInt, isRecommend = true))
        }
    }

    private fun createColorItem(colorInt: Int, isRecommend: Boolean): LinearLayout {
        val viewColor = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
            setBackgroundColor(colorInt)
        }

        val hexText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = String.format("#%06X", 0xFFFFFF and colorInt)
            textSize = 16f
            setPadding(32, 0, 0, 0)
            setTextColor(Color.parseColor("#444444"))
        }

        val item = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            setPadding(0, 8, 0, 8)
            addView(viewColor)
            addView(hexText)

            setOnClickListener {
                if (isRecommend) {
                    recommendSelectedView?.setBackgroundColor(Color.TRANSPARENT)
                    selectedItemView?.setBackgroundColor(Color.TRANSPARENT)

                    recommendSelectedView = this
                    recommendSelectedColor = colorInt
                    selectedColor = 0
                    selectedItemView = null

                    setBackgroundColor(Color.argb(40, 108, 75, 217))
                } else {
                    recommendSelectedView?.setBackgroundColor(Color.TRANSPARENT)
                    recommendSelectedColor = null

                    selectedItemView?.setBackgroundColor(Color.TRANSPARENT)
                    selectedItemView = this
                    selectedColor = colorInt

                    setBackgroundColor(Color.argb(40, 108, 75, 217))
                }
            }
        }

        return item
    }

    private fun updateKButtons() {
        fun tint(btn: Button, active: Boolean) {
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor(if (active) "#6C4BD9" else "#B39DDB")
            )
        }
        tint(buttonK3, currentK == 3)
        tint(buttonK5, currentK == 5)
        tint(buttonK8, currentK == 8)
    }
}
