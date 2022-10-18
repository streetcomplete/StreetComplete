package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.graphics.set
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogQrCodeBinding
import de.westnordost.streetcomplete.util.ktx.toast

class UrlConfigQRCodeDialog(
    context: Context,
    val url: String,
) : AlertDialog(context) {

    private val binding = DialogQrCodeBinding.inflate(LayoutInflater.from(context))

    init {
        binding.qrCodeView.doOnPreDraw { initializeQrCode() }

        binding.urlView.text = url
        binding.copyButton.setOnClickListener {
            val clipboard = context.getSystemService<ClipboardManager>()
            clipboard?.setPrimaryClip(ClipData.newPlainText("StreetComplete config URL", url))
            context.toast(R.string.urlconfig_url_copied)
        }

        setTitle(R.string.quest_presets_share)
        setView(binding.root)

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            dismiss()
        }
    }

    private fun initializeQrCode() {
        val qrCode = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 0, 0, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.QR_COMPACT to "true",
            EncodeHintType.MARGIN to 1
        )).toBitmap()

        val qrDrawable = BitmapDrawable(context.resources, qrCode)
        // scale QR image with "nearest neighbour", not with "linear" or whatever
        qrDrawable.paint.isFilterBitmap = false

        // force 1:1 aspect ratio
        binding.qrCodeView.updateLayoutParams {
            height = binding.qrCodeView.measuredWidth
        }
        binding.qrCodeView.setImageDrawable(qrDrawable)
    }
}

private fun BitMatrix.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap[x, y] = if (get(x, y)) Color.BLACK else Color.WHITE
        }
    }
    return bitmap
}
