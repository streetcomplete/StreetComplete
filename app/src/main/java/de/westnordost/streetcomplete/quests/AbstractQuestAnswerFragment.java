package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypes;
import de.westnordost.streetcomplete.data.meta.CountryInfo;
import de.westnordost.streetcomplete.data.meta.CountryInfos;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

/** Abstract base class for any dialog with which the user answers a specific quest(ion) */
public abstract class AbstractQuestAnswerFragment extends Fragment
{
	public static final String
			ARG_ELEMENT = "element",
			ARG_GEOMETRY = "geometry",
			ARG_QUESTTYPE = "quest_type";

	@Inject CountryInfos countryInfos;
	@Inject QuestTypes questTypes;

	private TextView title;
	private ViewGroup content;

	private LinearLayout bottomSheet;

	private QuestAnswerComponent questAnswerComponent;

	private LinearLayout buttonPanel;
	protected Button buttonOtherAnswers;

	private ImageButton buttonClose;

	private OsmElement osmElement;
	private ElementGeometry elementGeometry;
	private QuestType questType;
	private CountryInfo countryInfo;

	private List<OtherAnswer> otherAnswers;

	public AbstractQuestAnswerFragment()
	{
		super();
		Injector.instance.getApplicationComponent().inject(this);
		questAnswerComponent = new QuestAnswerComponent();
		otherAnswers = new ArrayList<>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		osmElement = (OsmElement) getArguments().getSerializable(ARG_ELEMENT);
		elementGeometry = (ElementGeometry) getArguments().getSerializable(ARG_GEOMETRY);
		questType = questTypes.forName(getArguments().getString(ARG_QUESTTYPE));
		countryInfo = null;

		View view = inflater.inflate(R.layout.quest_answer_fragment, container, false);

		bottomSheet = view.findViewById(R.id.bottomSheet);
		bottomSheet.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
									   int oldLeft, int oldTop, int oldRight, int oldBottom)
			{
				// not immediately because this is called during layout change (view.getTop() == 0)
				final Handler handler = new Handler();
				handler.post(new Runnable()
				{
					@Override public void run()
					{
						updateCloseButtonVisibility();
					}
				});
			}
		});

		title = view.findViewById(R.id.title);
		title.setText(getResources().getString(getQuestTitleResId(), getElementName()));

		buttonPanel = view.findViewById(R.id.buttonPanel);
		buttonOtherAnswers = buttonPanel.findViewById(R.id.buttonOtherAnswers);

		buttonClose = view.findViewById(R.id.close_btn);
		buttonClose.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				getActivity().onBackPressed();
			}
		});

		BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
		{
			@Override public void onStateChanged(@NonNull View bottomSheet, int newState) { }

			@Override public void onSlide(@NonNull View bottomSheet, float slideOffset)
			{
				updateCloseButtonVisibility();
			}
		});

		addOtherAnswer(R.string.quest_generic_answer_notApplicable, new Runnable()
		{
			@Override public void run()
			{
				onClickCantSay();
			}
		});

		content = view.findViewById(R.id.content);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		if(otherAnswers.size() == 1)
		{
			buttonOtherAnswers.setText(otherAnswers.get(0).titleResourceId);
			buttonOtherAnswers.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					otherAnswers.get(0).action.run();
				}
			});
		}
		else
		{
			buttonOtherAnswers.setText(R.string.quest_generic_otherAnswers);
			buttonOtherAnswers.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					PopupMenu popup = new PopupMenu(getActivity(), buttonOtherAnswers);
					for(int i = 0; i<otherAnswers.size(); ++i)
					{
						OtherAnswer otherAnswer = otherAnswers.get(i);
						int order = otherAnswers.size()-i;
						popup.getMenu().add(Menu.NONE, i, order, otherAnswer.titleResourceId);
					}
					popup.show();

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
					{
						@Override public boolean onMenuItemClick(MenuItem item)
						{
							otherAnswers.get(item.getItemId()).action.run();
							return true;
						}
					});
				}
			});
		}
	}

	private void updateCloseButtonVisibility()
	{
		// this is called asynchronously. It may happen that the activity is already gone when this
		// method is finally called
		if(getActivity() == null) return;

		int toolbarHeight = getActivity().findViewById(R.id.toolbar).getHeight();
		boolean coversToolbar = bottomSheet.getTop() < toolbarHeight;
		buttonClose.setVisibility(coversToolbar ? View.VISIBLE : View.GONE);
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
	}

	@Override public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) activity);
	}

	private String getElementName()
	{
		return osmElement != null && osmElement.getTags() != null ? osmElement.getTags().get("name") : null;
	}

	protected final void onClickCantSay()
	{
		DialogFragment leaveNote = new LeaveNoteDialog();
		Bundle leaveNoteArgs = questAnswerComponent.getArguments();
		String questTitle = getEnglishResources().getString(getQuestTitleResId(), getElementName());
		leaveNoteArgs.putString(LeaveNoteDialog.ARG_QUEST_TITLE, questTitle);
		leaveNote.setArguments(leaveNoteArgs);
		leaveNote.show(getFragmentManager(), null);
	}

	private Resources getEnglishResources()
	{
		Configuration conf = new Configuration(getResources().getConfiguration());
		conf.setLocale(Locale.ENGLISH);
		Context localizedContext = getActivity().createConfigurationContext(conf);
		return localizedContext.getResources();
	}

	/** Request to close the form through user interaction (back button, clicked other quest,..),
	 *  requires user confirmation if any changes have been made */
	@UiThread public void onClickClose(final Runnable confirmed)
	{
		if (!hasChanges())
		{
			confirmed.run();
		}
		else
		{
			DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					confirmed.run();
				}
			};
			new AlertDialogBuilder(getActivity())
					.setMessage(R.string.confirmation_discard_title)
					.setPositiveButton(R.string.confirmation_discard_positive, onYes)
					.setNegativeButton(R.string.confirmation_discard_negative, null)
					.show();
		}
	}

	protected final void applyImmediateAnswer(Bundle data)
	{
		questAnswerComponent.onAnswerQuest(data);
	}

	protected final void skipQuest()
	{
		questAnswerComponent.onSkippedQuest();
	}

	private int getQuestTitleResId()
	{
		if(questType instanceof OsmElementQuestType)
		{
			Map<String,String> tags = Collections.emptyMap();
			if(osmElement != null && osmElement.getTags() != null)
			{
				tags = osmElement.getTags();
			}
			return ((OsmElementQuestType) questType).getTitle(tags);
		}
		return questType.getTitle();
	}

	protected final View setContentView(int resourceId)
	{
		if(content.getChildCount() > 0)
		{
			content.removeAllViews();
		}
		return getActivity().getLayoutInflater().inflate(resourceId, content);
	}

	protected final View setButtonsView(int resourceId)
	{
		return getActivity().getLayoutInflater().inflate(resourceId, buttonPanel);
	}

	public abstract boolean hasChanges();

	public final long getQuestId()
	{
		return questAnswerComponent.getQuestId();
	}

	public final QuestGroup getQuestGroup()
	{
		return questAnswerComponent.getQuestGroup();
	}

	protected final OsmElement getOsmElement()
	{
		return osmElement;
	}

	protected final ElementGeometry getElementGeometry()
	{
		return elementGeometry;
	}

	protected final CountryInfo getCountryInfo()
	{
		// cache it
		if(countryInfo != null) return countryInfo;

		LatLon latLon = elementGeometry.center;
		countryInfo = countryInfos.get(latLon.getLongitude(), latLon.getLatitude());
		return countryInfo;
	}

	protected final void addOtherAnswer(int titleResourceId, Runnable action)
	{
		OtherAnswer oa = new OtherAnswer();
		oa.titleResourceId = titleResourceId;
		oa.action = action;
		otherAnswers.add(oa);
	}

	private class OtherAnswer
	{
		int titleResourceId;
		Runnable action;
	}
}
