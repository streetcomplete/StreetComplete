package de.westnordost.streetcomplete.quests;

import android.content.ContextWrapper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.meta.CountryInfo;
import de.westnordost.streetcomplete.data.meta.CountryInfos;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;

/** Abstract base class for any dialog with which the user answers a specific quest(ion) */
public abstract class AbstractQuestAnswerFragment extends AbstractBottomSheetFragment
{
	public static final String
			ARG_ELEMENT = "element",
			ARG_GEOMETRY = "geometry",
			ARG_QUESTTYPE = "quest_type",
			ARG_MAP_ROTATION = "map_rotation",
			ARG_MAP_TILT = "map_tilt";

	@Inject CountryInfos countryInfos;
	@Inject QuestTypeRegistry questTypeRegistry;

	private ViewGroup content;

	private final QuestAnswerComponent questAnswerComponent;

	private ViewGroup buttonPanel;
	protected Button buttonOtherAnswers;

	private OsmElement osmElement;
	private ElementGeometry elementGeometry;
	private QuestType questType;
	private CountryInfo countryInfo;

	private float initialMapRotation, initialMapTilt;

	private List<OtherAnswer> otherAnswers;

	private WeakReference<Context> currentContext = new WeakReference<>(null);
	private ContextWrapper currentCountryContext;

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
		otherAnswers = new ArrayList<>();
		View view = inflater.inflate(R.layout.fragment_quest_answer, container, false);

		TextView title = view.findViewById(R.id.title);
		title.setText(QuestUtil.getHtmlTitle(getResources(), questType, osmElement));

		buttonPanel = view.findViewById(R.id.buttonPanel);
		buttonOtherAnswers = buttonPanel.findViewById(R.id.buttonOtherAnswers);

		addOtherAnswer(R.string.quest_generic_answer_notApplicable, this::onClickCantSay);

		content = view.findViewById(R.id.content);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// no content? -> hide the content container
		if(content.getChildCount() == 0)
		{
			content.setVisibility(View.GONE);
		}

		if(otherAnswers.size() == 1)
		{
			buttonOtherAnswers.setText(otherAnswers.get(0).titleResourceId);
			buttonOtherAnswers.setOnClickListener(v -> otherAnswers.get(0).action.run());
		}
		else
		{
			buttonOtherAnswers.setText(R.string.quest_generic_otherAnswers);
			buttonOtherAnswers.setOnClickListener(v ->
			{
				PopupMenu popup = new PopupMenu(getActivity(), buttonOtherAnswers);
				for(int i = 0; i<otherAnswers.size(); ++i)
				{
					OtherAnswer otherAnswer = otherAnswers.get(i);
					int order = otherAnswers.size()-i;
					popup.getMenu().add(Menu.NONE, i, order, otherAnswer.titleResourceId);
				}
				popup.show();

				popup.setOnMenuItemClickListener(item ->
				{
					otherAnswers.get(item.getItemId()).action.run();
					return true;
				});
			});
		}

		onMapOrientation(initialMapRotation, initialMapTilt);
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
		osmElement = (OsmElement) getArguments().getSerializable(ARG_ELEMENT);
		elementGeometry = (ElementGeometry) getArguments().getSerializable(ARG_GEOMETRY);
		questType = questTypeRegistry.getByName(getArguments().getString(ARG_QUESTTYPE));
		countryInfo = null;
		initialMapRotation = getArguments().getFloat(ARG_MAP_ROTATION);
		initialMapTilt = getArguments().getFloat(ARG_MAP_TILT);
	}

	@Override public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	/** Note: Due to Android architecture limitations, a layout inflater based on this ContextWrapper
	 *  will not resolve any resources specified in the XML according to MCC */
	@NonNull @Override
	public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState)
	{
		// will always return a layout inflater for the current country
		return super.onGetLayoutInflater(savedInstanceState).cloneInContext(getContext());
	}

	@Override @Nullable public Context getContext()
	{
		Context context = super.getContext();
		if(currentContext.get() != context)
		{
			currentContext = new WeakReference<>(context);
			currentCountryContext = context != null ? createCurrentCountryContextWrapper(context) : null;
		}
		return currentCountryContext;
	}

	private ContextWrapper createCurrentCountryContextWrapper(Context context)
	{
		Configuration conf = new Configuration(context.getResources().getConfiguration());
		Integer mcc = getCountryInfo().getMobileCountryCode();
		conf.mcc = mcc != null ? mcc : 0;
		Resources res = context.createConfigurationContext(conf).getResources();
		return new ContextWrapper(context) {
			@Override public Resources getResources()
			{
				return res;
			}
		};
	}

	protected final void onClickCantSay()
	{
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.quest_leave_new_note_title)
			.setMessage(R.string.quest_leave_new_note_description)
			.setNegativeButton(R.string.quest_leave_new_note_no, (dialog, which) ->
			{
				questAnswerComponent.onSkippedQuest();
			})
			.setPositiveButton(R.string.quest_leave_new_note_yes, ((dialog, which) ->
			{
				String questTitle = QuestUtil.getTitle(getEnglishResources(), questType, osmElement);
				questAnswerComponent.onComposeNote(questTitle);
			}))
		.show();
	}

	private Resources getEnglishResources()
	{
		Configuration conf = new Configuration(getResources().getConfiguration());
		conf.setLocale(Locale.ENGLISH);
		Context localizedContext = super.getContext().createConfigurationContext(conf);
		return localizedContext.getResources();
	}

	protected final void applyAnswer(@NonNull Bundle data)
	{
		if(data.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		// in case there is a bug that a quest fills the bundle with nulls -> report
		for(String key : data.keySet())
		{
			if(data.get(key) == null) throw new NullPointerException("Key " + key + " is null");
		}
		questAnswerComponent.onAnswerQuest(data);
	}

	protected final void skipQuest()
	{
		questAnswerComponent.onSkippedQuest();
	}

	protected final View setContentView(int resourceId)
	{
		if(content.getChildCount() > 0)
		{
			content.removeAllViews();
		}
		content.setVisibility(View.VISIBLE);
		return getLayoutInflater().inflate(resourceId, content);
	}

	protected final void setNoContentPadding()
	{
		content.setPadding(0, 0, 0, 0);
	}

	protected final View setButtonsView(int resourceId)
	{
		// if other buttons are present, the other answers button should have a weight so that it
		// can be sqeezed if there is not enough space for everything
		buttonOtherAnswers.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		return getActivity().getLayoutInflater().inflate(resourceId, buttonPanel);
	}

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

	@AnyThread public void onMapOrientation(float rotation, float tilt)
	{
		// default empty implementation
	}

	private static class OtherAnswer
	{
		int titleResourceId;
		Runnable action;
	}
}
