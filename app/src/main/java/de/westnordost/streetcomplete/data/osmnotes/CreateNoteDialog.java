package de.westnordost.streetcomplete.data.osmnotes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.QuestAnswerComponent;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;


public class CreateNoteDialog extends DialogFragment
{

	private EditText noteInput;

	private QuestAnswerComponent questAnswerComponent;

	public static final String ARG_LAT = "latitude";
	public static final String ARG_LON = "longitude";

	public CreateNoteDialog()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_create_note, container, false);

		view.findViewById(R.id.buttonCancel).setOnClickListener(v -> onClickCancel());
		view.findViewById(R.id.buttonOk).setOnClickListener(v -> onClickOk());

		noteInput = view.findViewById(R.id.noteInput);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null)
		{
			getChildFragmentManager().beginTransaction().add(R.id.attachPhotoFragment, new AttachPhotoFragment()).commit();
		}
	}

	private @Nullable AttachPhotoFragment getAttachPhotoFragment()
	{
		return (AttachPhotoFragment) getChildFragmentManager().findFragmentById(R.id.attachPhotoFragment);
	}

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();

		if(noteText.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		AttachPhotoFragment f = getAttachPhotoFragment();

		Double latitude = getArguments().getDouble(ARG_LAT);
		Double longitude = getArguments().getDouble(ARG_LON);
		LatLon position = new OsmLatLon(latitude, longitude);

		questAnswerComponent.onLeaveNote(noteText, f != null ? f.getImagePaths() : null, position);
		dismiss();
	}

	private void onClickCancel()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
		dismiss();
	}
}
