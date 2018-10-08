package de.westnordost.streetcomplete.data.osmnotes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.westnordost.streetcomplete.util.ImageUploader;

public class AttachPhotoUtils
{
	public static String uploadAndGetAttachedPhotosText(ImageUploader imageUploader, List<String> imagePaths)
	{
		if(imagePaths != null && !imagePaths.isEmpty())
		{
			List<String> urls = imageUploader.upload(imagePaths);
			if (urls != null && !urls.isEmpty())
			{
				StringBuilder sb = new StringBuilder("\n\nAttached photo(s):");
				for(String link : urls)
				{
					sb.append("\n");
					sb.append(link);
				}
				return sb.toString();
			}
		}
		return "";
	}

	public static void deleteImages(List<String> imagePaths)
	{
		if(imagePaths != null)
		{
			for (String path : imagePaths)
			{
				if(path != null)
				{
					File file = new File(path);
					if (file.exists())
					{
						file.delete();
					}
				}
			}
		}
	}

	public static Bitmap resize(String imagePath, int maxWidth)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;

		if(srcWidth <= 0 || srcHeight <= 0) return null;

		// Only resize if the source is big enough. This code is just trying to fit a image into a certain width.
		if(maxWidth > srcWidth)
			maxWidth = srcWidth;

		// Calculate the correct inSampleSize/resize value. This helps reduce memory use. It should be a power of 2
		// from: https://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
		int inSampleSize = 1;
		while(srcWidth / 2 > maxWidth){
			srcWidth /= 2;
			srcHeight /= 2;
			inSampleSize *= 2;
		}

		float desiredScale = (float) maxWidth / srcWidth;

		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = inSampleSize;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(imagePath, options);

		// Resize & Rotate
		Matrix matrix = getRotationMatrix(imagePath);
		matrix.postScale(desiredScale, desiredScale);
		Bitmap result = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);

		if(result != sampledSrcBitmap)
		{
			sampledSrcBitmap.recycle();
		}
		return result;
	}

	private static Matrix getRotationMatrix(String imagePath)
	{
		int orientation = 0;
		try
		{
			ExifInterface exifInterface = new ExifInterface(imagePath);
			orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_UNDEFINED);
		}
		catch (IOException ignore)
		{
		}

		Matrix matrix = new Matrix();
		switch (orientation)
		{
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(-90);
				break;
		}
		return matrix;
	}
}
