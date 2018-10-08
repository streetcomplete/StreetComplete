package de.westnordost.streetcomplete.quests.segregated;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddCyclewaySegregation extends SimpleOverpassQuestType {

	@Inject
	public AddCyclewaySegregation(OverpassMapDataDao overpassServer) {
		super(overpassServer);
	}

	@Override
	protected String getTagFilters() {
		return
			"ways with " +
				"(" +
				"(highway = path and bicycle = designated and foot = designated)" +
				" or (highway = footway and bicycle = designated)" +
				" or (highway = cycleway and foot ~ designated|yes)" +
				")" +
				" and surface ~" + TextUtils.join("|", OsmTaggings.ANYTHING_PAVED) +
				" and !segregated";
	}

	@Override
	public AbstractQuestAnswerFragment createForm() {
		return new AddCyclewaySegregationForm();
	}

	@Override
	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) {
		List<String> values = answer.getStringArrayList(AddCyclewaySegregationForm.OSM_VALUES);
		if (values != null && values.size() == 1) {
			changes.add("segregated", values.get(0));
		}
	}

	@Override
	public String getCommitMessage() {
		return "Add segregated status for combined footway with cycleway";
	}

	@Override
	public int getIcon() {
		return R.drawable.ic_quest_path_segregation;
	}

	@Override
	public int getTitle(@NonNull Map<String, String> tags) {
		return R.string.quest_segregated_title;
	}
}
