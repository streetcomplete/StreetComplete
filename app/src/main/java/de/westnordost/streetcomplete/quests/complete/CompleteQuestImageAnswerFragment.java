package de.westnordost.streetcomplete.quests.complete;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;

public class CompleteQuestImageAnswerFragment extends AbstractQuestFormAnswerFragment
{
	public static final String IMAGE_PATHS = "image_paths";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		buttonOtherAnswers.setVisibility(View.GONE);
		setContentView(R.layout.quest_complete_image);

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

	@Override protected void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		AttachPhotoFragment f = getAttachPhotoFragment();

		Bundle answer = new Bundle();
		answer.putStringArrayList(IMAGE_PATHS, f != null ? f.getImagePaths() : null);
		applyImmediateAnswer(answer);
	}

	@Override
	public void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
	}

	@Override public boolean hasChanges()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		return f != null && !f.getImagePaths().isEmpty();
	}
}
