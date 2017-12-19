package de.westnordost.streetcomplete.data.osmnotes;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import de.westnordost.streetcomplete.MainActivity;
import de.westnordost.streetcomplete.R;

public class CreateNoteFragment extends Fragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_create_note, container, false);

		Toolbar noteToolbar = view.findViewById(R.id.note_toolbar);
		((AppCompatActivity)getActivity()).setSupportActionBar(noteToolbar);

		ImageButton finishNoteCreation = view.findViewById(R.id.finish_note_creation);
		ImageButton closeNoteCreation = view.findViewById(R.id.close_note_creation);

		finishNoteCreation.setOnClickListener((v) -> openDialog());

		closeNoteCreation.setOnClickListener((v) ->
		{
			getActivity().getSupportFragmentManager().popBackStackImmediate(MainActivity.CREATE_NOTE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		});

		return view;
	}

	private void openDialog()
	{
		DialogFragment createNoteDialog = new CreateNoteDialog();

		Bundle createNoteArgs = new Bundle();
		createNoteArgs.putDouble(CreateNoteDialog.ARG_LAT, getArguments().getDouble(CreateNoteDialog.ARG_LAT));
		createNoteArgs.putDouble(CreateNoteDialog.ARG_LON, getArguments().getDouble(CreateNoteDialog.ARG_LAT));
		createNoteDialog.setArguments(createNoteArgs);

		createNoteDialog.show(getActivity().getSupportFragmentManager(), null);
	}
}