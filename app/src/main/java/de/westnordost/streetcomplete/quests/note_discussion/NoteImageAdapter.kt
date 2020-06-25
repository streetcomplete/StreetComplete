package de.westnordost.streetcomplete.quests.note_discussion

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnLayout

import java.io.File

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.decodeScaledBitmapAndNormalize
import de.westnordost.streetcomplete.view.ListAdapter

class NoteImageAdapter(list: List<String>, private val context: Context) : ListAdapter<String>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<String> {
        val inflater = LayoutInflater.from(parent.context)
        return NoteImageViewHolder(inflater.inflate(R.layout.cell_image_thumbnail, parent, false))
    }

    private inner class NoteImageViewHolder(itemView: View) : ViewHolder<String>(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            imageView.setOnClickListener {
                val index = adapterPosition
                if (index > -1) onClickDelete(index)
            }
        }

        override fun onBind(with: String) {
            itemView.doOnLayout {
                val bitmap = decodeScaledBitmapAndNormalize(with, imageView.width, imageView.height)
                imageView.setImageBitmap(bitmap)
            }
        }
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
