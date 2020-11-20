package de.westnordost.streetcomplete.data.upload;

import java.util.Arrays;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementsUploader;
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsUploader;
import de.westnordost.streetcomplete.data.osm.splitway.SplitWaysUploader;
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestsUploader;
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNotesUploader;
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsChangesUploader;

@Module
public class UploadModule2
{
	/* NOTE: For some reason, when converting this to Kotlin, Dagger 2 does not find this anymore
	*  and cannot provide the dependency for UploadService. So, it must stay in Java (for now) */
	@Provides public static List<? extends Uploader> uploaders(
		OsmNoteQuestsChangesUploader osmNoteQuestsChangesUploader,
		DeleteOsmElementsUploader deleteOsmElementsUploader,
		UndoOsmQuestsUploader undoOsmQuestsUploader,
		OsmQuestsUploader osmQuestsUploader,
		SplitWaysUploader splitWaysUploader,
		CreateNotesUploader createNotesUploader
	) {
		return Arrays.asList(
				osmNoteQuestsChangesUploader,
				deleteOsmElementsUploader,
				/* the order is important: undo should happen before normal quest changes are
				   uploaded, so that new quests can be unlocked (not-uploaded undos block creation
				   of new quests) on upload of these */
				undoOsmQuestsUploader,
				osmQuestsUploader,
				splitWaysUploader,
				createNotesUploader
		);
	}
}
