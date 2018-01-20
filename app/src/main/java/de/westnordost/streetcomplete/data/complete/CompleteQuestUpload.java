package de.westnordost.streetcomplete.data.complete;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import de.westnordost.streetcomplete.data.QuestStatus;

import static de.westnordost.streetcomplete.ApplicationConstants.COMPLETE_AUTH;
import static de.westnordost.streetcomplete.ApplicationConstants.COMPLETE_INSTANCE;

public class CompleteQuestUpload
{
	private static final String TAG = "CompleteQuestUpload";

	private final CompleteQuestDao completeQuestDB;

	@Inject public CompleteQuestUpload(CompleteQuestDao completeQuestDB)
	{
		this.completeQuestDB = completeQuestDB;
	}

	public void upload(AtomicBoolean cancelState)
	{
		int questions = 0;
		for(CompleteQuest complete : completeQuestDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break;

			if(uploadCompleteAnswer(complete) != null)
			{
				questions++;
			}
		}
		String logMsg = "Uploaded " + questions + " CompleteStreetComplete questions";
		Log.i(TAG, logMsg);
	}

	private CompleteQuest uploadCompleteAnswer(CompleteQuest quest)
	{
		uploadToServer(quest.getComplete().apiId, quest.getComplete().answer, quest.getComplete().country);
		//hide the quest
		CompleteQuest q = completeQuestDB.get(quest.getId());
		q.setStatus(QuestStatus.HIDDEN);
		completeQuestDB.update(q);

		return q;
	}

	private void uploadToServer(Long id, String answer, String country)
	{
		String url = COMPLETE_INSTANCE + "api/post/" + String.valueOf(id);
		try
		{
			URL uri = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			String data = "{\"values\":{\"" + country + "\":\"" + answer + "\"}}";

			String encoding = Base64.encodeToString(COMPLETE_AUTH.getBytes(), Base64.DEFAULT);
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			byte[] out = data.getBytes();
			int length = out.length;

			connection.setFixedLengthStreamingMode(length);
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.connect();
			try(OutputStream os = connection.getOutputStream()) {
				os.write(out);
			}

			BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = serverAnswer.readLine()) != null) {
				System.out.println("LINE: " + line);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
