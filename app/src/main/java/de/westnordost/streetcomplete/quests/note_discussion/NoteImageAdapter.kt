package de.westnordost.streetcomplete.quests.note_discussion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.CellImageThumbnailBinding
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import de.westnordost.streetcomplete.util.getRotationMatrix
import de.westnordost.streetcomplete.view.ListAdapter
import com.github.chrisbanes.photoview.PhotoView
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
        if (!image.exists()) return // delete from list?
        val v = PhotoView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val matrix = getRotationMatrix(imagePath)
            val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (result != bitmap) {
                bitmap.recycle()
            }
            setImageBitmap(result)
        }
        AlertDialog.Builder(context)
            .setView(v)
            .setNegativeButton(R.string.attach_photo_delete) { _, _ -> delete(index) }
            .setPositiveButton(android.R.string.ok, null)
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
