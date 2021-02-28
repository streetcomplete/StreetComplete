package de.westnordost.streetcomplete.data.upload;

import java.util.Arrays;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader;
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsUploader;

@Module
public class UploadModule2
{
	/* NOTE: For some reason, when converting this to Kotlin, Dagger 2 does not find this anymore
	*  and cannot provide the dependency for UploadService. So, it must stay in Java (for now) */
	@Provides public static List<? extends Uploader> uploaders(
		NoteEditsUploader noteEditsUploader,
		ElementEditsUploader elementEditsUploader
	) {
		return Arrays.asList(
				noteEditsUploader,
				elementEditsUploader
		);
	}
}
