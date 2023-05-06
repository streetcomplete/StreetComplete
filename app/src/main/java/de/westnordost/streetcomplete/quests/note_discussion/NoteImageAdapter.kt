package de.westnordost.streetcomplete.quests.note_discussion

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import com.github.chrisbanes.photoview.PhotoView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.CellImageThumbnailBinding
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import de.westnordost.streetcomplete.util.getRotationMatrix
import de.westnordost.streetcomplete.view.ListAdapter
import java.io.File

class NoteImageAdapter(list: List<String>, private val context: Context) : ListAdapter<String>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<String> {
        val inflater = LayoutInflater.from(parent.context)
        return NoteImageViewHolder(CellImageThumbnailBinding.inflate(inflater, parent, false))
    }

    private inner class NoteImageViewHolder(val binding: CellImageThumbnailBinding) : ViewHolder<String>(binding) {

        init {
            binding.imageView.setOnClickListener {
                val index = adapterPosition
                if (index > -1) onClickImage(index)
            }
            binding.deleteButton.setOnClickListener {
                val index = adapterPosition
                if (index > -1) onClickDelete(index)
            }
        }

        override fun onBind(with: String) {
            binding.root.doOnLayout {
                val bitmap = decodeScaledBitmapAndNormalize(with, binding.imageView.width, binding.imageView.height)
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun onClickImage(index: Int) {
        val imagePath = list[index]
        val image = File(imagePath)
        if (!image.exists()) return

        val dialog = Dialog(context)
        val imageView = PhotoView(context)
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val matrix = getRotationMatrix(imagePath)
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (result != bitmap) {
            bitmap.recycle()
        }
        imageView.setImageBitmap(result)
        imageView.setOnOutsidePhotoTapListener { dialog.dismiss() }
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.minimumScale = 0.75f
        imageView.maximumScale = 4f
        imageView.doOnLayout { imageView.scale = 0.75f }
        dialog.setContentView(imageView)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun onClickDelete(index: Int) {
        AlertDialog.Builder(context)
            .setMessage(R.string.quest_leave_new_note_photo_delete_title)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> delete(index) }
            .show()
    }

    private fun delete(index: Int) {
        val imagePath = list.removeAt(index)
        val image = File(imagePath)
        if (image.exists()) {
            image.delete()
        }
        notifyItemRemoved(index)
    }
}
