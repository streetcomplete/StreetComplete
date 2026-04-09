package de.westnordost.streetcomplete.util.photo

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.TAG_GPS_IMG_DIRECTION
import androidx.exifinterface.media.ExifInterface.TAG_GPS_IMG_DIRECTION_REF
import de.westnordost.streetcomplete.ApplicationConstants.ATTACH_PHOTO_MAX_SIZE
import de.westnordost.streetcomplete.ApplicationConstants.ATTACH_PHOTO_QUALITY
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun createPhotoFile(context: Context) : File {
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?:
        throw IOException("Shared storage is not available")
    val imageFileName = "photo_" + nowAsEpochMilliseconds() + ".jpg"
    val file = File(directory, imageFileName)
    if (!file.createNewFile()) throw IOException("Photo file with exactly the same name already exists")
    return file
}

/**
 * Rescales photo and saves as JPEG with OK quality, keeps only the GPS image direction EXIF data.
 * */
fun rescalePhoto(file: File) {
    val path = file.path
    val originalExif = ExifInterface(path)

    val bitmap = decodeScaledBitmapAndNormalize(path, ATTACH_PHOTO_MAX_SIZE, ATTACH_PHOTO_MAX_SIZE)
        ?: throw IOException()

    bitmap.compress(Bitmap.CompressFormat.JPEG, ATTACH_PHOTO_QUALITY, FileOutputStream(path))

    val newExif = ExifInterface(path)
    newExif.setAttribute(TAG_GPS_IMG_DIRECTION, originalExif.getAttribute(TAG_GPS_IMG_DIRECTION))
    newExif.setAttribute(TAG_GPS_IMG_DIRECTION_REF, originalExif.getAttribute(TAG_GPS_IMG_DIRECTION_REF))
    newExif.saveAttributes()
}
