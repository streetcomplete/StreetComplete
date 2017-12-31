package de.westnordost.streetcomplete.data.osmnotes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.MainActivity;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;


public class CreateNoteDialog extends AbstractQuestAnswerFragment
{

	private EditText noteInput;

	private CreateNoteAnswerComponent createNoteAnswerComponent;

	public static final String ARG_LAT = "latitude";
	public static final String ARG_LON = "longitude";

	public CreateNoteDialog()
	{
		super();
		createNoteAnswerComponent = new CreateNoteAnswerComponent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.create_note);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_ok_cancel);

		buttonPanel.findViewById(R.id.buttonCancel).setOnClickListener(v -> onClickCancel());
		buttonPanel.findViewById(R.id.buttonOk).setOnClickListener(v -> onClickOk());

		noteInput = contentView.findViewById(R.id.noteInput);

		buttonOtherAnswers.setVisibility(View.GONE);

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

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		createNoteAnswerComponent.onAttach((CreateNoteListener) ctx);
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

		createNoteAnswerComponent.onLeaveNote(noteText, f != null ? f.getImagePaths() : null, position);

		getActivity().getSupportFragmentManager().popBackStackImmediate(MainActivity.BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	private void onClickCancel()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();

		getActivity().getSupportFragmentManager().popBackStackImmediate(MainActivity.BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Override public boolean hasChanges()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}

	@Override protected int getQuestTitleResId()
	{
		return R.string.action_note;
	}
}
