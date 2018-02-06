package de.westnordost.streetcomplete.quests.note_discussion;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.AttachPhotoUtils;

import static android.app.Activity.RESULT_OK;

public class AttachPhotoFragment extends Fragment
{
	private ArrayList<String> imagePaths;
	private String currentImagePath;

	private NoteImageAdapter noteImageAdapter;

	private static final String TAG = "AttachPhotoFragment";
	private static final int REQUEST_TAKE_PHOTO = 1;

	private static final String PHOTO_PATHS = "photo_paths";
	private static final String CURRENT_PHOTO_PATH = "current_photo_path";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_attach_photo, container, false);

		ImageButton takePhoto = view.findViewById(R.id.buttonTakeImage);
		takePhoto.setOnClickListener(v -> takePhoto());

		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			view.setVisibility(View.GONE);
		}

		if (savedInstanceState != null)
		{
			imagePaths = savedInstanceState.getStringArrayList(PHOTO_PATHS);
			currentImagePath = savedInstanceState.getString(CURRENT_PHOTO_PATH);
		}
		else
		{
			imagePaths = new ArrayList<>();
			currentImagePath = null;
		}

		noteImageAdapter = new NoteImageAdapter(imagePaths, getContext());
		RecyclerView gridView = view.findViewById(R.id.gridView);
		gridView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		gridView.setAdapter(noteImageAdapter);

		return view;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(PHOTO_PATHS, imagePaths);
		outState.putString(CURRENT_PHOTO_PATH, currentImagePath);
	}

	private void takePhoto()
	{
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null)
		{
			try
			{
				File photoFile = createImageFile();
				Uri photoUri;
				if (Build.VERSION.SDK_INT > 21)
				{
					//Use FileProvider for getting the content:// URI, see: https://developer.android.com/training/camera/photobasics.html#TaskPath
					photoUri = FileProvider.getUriForFile(getActivity(), getString(R.string.fileprovider_authority), photoFile);
				}
				else
				{
					photoUri = Uri.fromFile(photoFile);
				}
				currentImagePath = photoFile.getPath();
				takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
				startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
			}
			catch (IOException | IllegalArgumentException e)
			{
				Log.e(TAG, "Unable to create file for photo", e);
				Toast.makeText(getContext(), R.string.quest_leave_new_note_create_image_error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_TAKE_PHOTO)
		{
			if (resultCode == RESULT_OK)
			{
				try
				{
					Bitmap bitmap = AttachPhotoUtils.resize(currentImagePath, ApplicationConstants.ATTACH_PHOTO_MAXWIDTH);
					if(bitmap == null) throw new IOException();
					FileOutputStream out = new FileOutputStream(currentImagePath);
					bitmap.compress(Bitmap.CompressFormat.JPEG, ApplicationConstants.ATTACH_PHOTO_QUALITY, out);

					imagePaths.add(currentImagePath);
					noteImageAdapter.notifyItemInserted(imagePaths.size()-1);
				}
				catch (IOException e)
				{
					Log.e(TAG, "Unable to rescale the photo", e);
					Toast.makeText(getContext(), R.string.quest_leave_new_note_create_image_error, Toast.LENGTH_SHORT).show();
					removeCurrentImage();
				}
			}
			else
			{
				removeCurrentImage();
			}
			currentImagePath = null;
		}
	}

	private void removeCurrentImage()
	{
		if(currentImagePath != null)
		{
			File photoFile = new File(currentImagePath);
			if (photoFile.exists())
			{
				photoFile.delete();
			}
		}
	}

	private File createImageFile() throws IOException
	{
		File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return File.createTempFile("photo", ".jpg", directory);
	}

	public ArrayList<String> getImagePaths()
	{
		return imagePaths;
	}

	public void deleteImages()
	{
		AttachPhotoUtils.deleteImages(imagePaths);
	}
}
