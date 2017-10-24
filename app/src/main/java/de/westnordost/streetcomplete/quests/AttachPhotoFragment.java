package de.westnordost.streetcomplete.quests;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm;
import de.westnordost.streetcomplete.view.NoteImageAdapter;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;

public class AttachPhotoFragment extends Fragment
{
	private ArrayList<String> imagePaths = new ArrayList<>();
	private ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

	File photoFile = null;

	static final int REQUEST_TAKE_PHOTO = 1;

	GridView gridView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		final View panel = inflater.inflate(R.layout.attach_photo_fragment, container, false);

		Button takePhoto = panel.findViewById(R.id.buttonTakeImage);
		takePhoto.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				takePhoto();
			}
		});

		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			takePhoto.setVisibility(View.GONE);
		}

		final NoteImageAdapter noteImageAdapter = new NoteImageAdapter(getActivity(), imageBitmaps);
		gridView = panel.findViewById(R.id.gridView);
		gridView.setAdapter(noteImageAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
			{
				new AlertDialogBuilder(getActivity())
						.setMessage(R.string.quest_leave_new_note_photo_delete_title)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								imageBitmaps.remove(position);
								imagePaths.remove(position);
								noteImageAdapter.notifyDataSetChanged();
							}
						})
						.show();
			}
		});

		return panel;
	}

	private void takePhoto()
	{
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			try {
				photoFile = createImageFile();
			} catch (IOException e) {
			}
			if (photoFile != null) {
				if (Build.VERSION.SDK_INT > 21) {
					//Use FileProvider for getting the content:// URI, see: https://developer.android.com/training/camera/photobasics.html#TaskPath
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(getActivity(), "de.westnordost.streetcomplete.fileprovider", photoFile));
				} else {
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				}
				startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == RESULT_OK)
			{
				imageBitmaps.add(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoFile.getPath()), 150, 150));
				imagePaths.add(photoFile.toString());

				LeaveNoteDialog.imagePaths = imagePaths;
				NoteDiscussionForm.imagePaths = imagePaths;
			} else
			{
				if (photoFile.exists())
				{
					photoFile.delete();
				}
			}
		}
	}

	private File createImageFile() throws IOException
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return File.createTempFile(
				imageFileName,
				".jpg",
				directory
		);
	}

	public void deleteImages(ArrayList<String> imagesForDeletetion)
	{
		if(imagesForDeletetion != null)
		{
			for (String path : imagesForDeletetion)
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
