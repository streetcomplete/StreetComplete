package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.NoteImageAdapter;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;

public class LeaveNoteDialog extends DialogFragment
{
	public static final String ARG_QUEST_TITLE = "questTitle";

	private EditText noteInput;
	private Button buttonOk;

	private QuestAnswerComponent questAnswerComponent;

	private String questTitle;

	private ArrayList<String> imagePaths = new ArrayList<>();
	private ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

	File photoFile = null;

	static final int REQUEST_TAKE_PHOTO = 1;

	GridView gridView;

	public LeaveNoteDialog()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.leave_note, container, false);

		Button buttonCancel = view.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickCancel();
			}
		});
		buttonOk = view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickOk();
			}
		});
		Button takePhoto = view.findViewById(R.id.buttonTakeImage);
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
		gridView = view.findViewById(R.id.gridView);
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

		noteInput = view.findViewById(R.id.noteInput);

		return view;
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		setStyle(STYLE_NO_TITLE,R.style.AppTheme_AlertDialog);
		questAnswerComponent.onCreate(getArguments());
		questTitle = getArguments().getString(ARG_QUEST_TITLE);
	}

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) activity);
	}

	private void onClickOk()
	{
		String inputText = noteInput.getText().toString().trim();
		if(inputText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}

		questAnswerComponent.onLeaveNote(questTitle, inputText, imagePaths);
		dismiss();
	}

	private void onClickCancel()
	{
		questAnswerComponent.onSkippedQuest();
		dismiss();
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
				Log.d("photoFile", photoFile.toString());
				if (Build.VERSION.SDK_INT > 21) {
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
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			imageBitmaps.add(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
			imagePaths.add(photoFile.toString());
		}
	}

	private File createImageFile() throws IOException
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";

		File file = new File(getActivity().getFilesDir() + File.separator + "images" + File.separator + imageFileName + ".jpg");
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.close();

		return file;
	}
}
