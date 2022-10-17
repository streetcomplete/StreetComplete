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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.westnordost.streetcomplete.databinding.DialogQrCodeBinding
import org.koin.core.component.KoinComponent

class UrlConfigQRCodeDialog(
    context: Context,
    val url: String,
) : AlertDialog(context), KoinComponent {

    private val binding = DialogQrCodeBinding.inflate(LayoutInflater.from(context))

    init {
        val qrCode = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 0, 0, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.QR_COMPACT to "true",
            EncodeHintType.MARGIN to 0 // the view itself has already a margin
        )).toBitmap()

        val qrDrawable = BitmapDrawable(context.resources, qrCode)
        // scale QR image with "nearest neighbour", not with "linear" or whatever
        qrDrawable.paint.isFilterBitmap = false

        binding.qrCodeView.setImageDrawable(qrDrawable)
        binding.urlView.setText(url)
        binding.copyButton.setOnClickListener {
            val clipboard = context.getSystemService<ClipboardManager>()
            clipboard?.setPrimaryClip(ClipData.newPlainText("StreetComplete config URL", url))
        }

        setView(binding.root)

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            dismiss()
        }
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
