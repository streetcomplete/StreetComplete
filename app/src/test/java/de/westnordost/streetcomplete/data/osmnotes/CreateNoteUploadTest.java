package de.westnordost.streetcomplete.data.osmnotes;

import junit.framework.TestCase;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.map.data.BoundingBox;

public class CreateNoteUploadTest extends TestCase
{
	public void testCancel() throws InterruptedException
	{
		CreateNoteDao createNoteDao = Mockito.mock(CreateNoteDao.class);
		Mockito.when(createNoteDao.getAll(Mockito.any(BoundingBox.class))).thenAnswer(
				new Answer<List<CreateNote>>()
		{
			@Override public List<CreateNote> answer(InvocationOnMock invocation) throws Throwable
			{
				Thread.sleep(1000); // take your time...
				ArrayList<CreateNote> result = new ArrayList<>();
				result.add(new CreateNote());
				return result;
			}
		});

		final CreateNoteUpload u = new CreateNoteUpload(createNoteDao,null,null,null,null);
		final AtomicBoolean cancel = new AtomicBoolean(false);
		Thread t = new Thread(new Runnable()
		{
			@Override public void run()
			{
				u.upload(cancel);
			}
		});
		t.start();

		cancel.set(true);
		// cancelling the thread works if we come out here without exceptions. If the note upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it only with nulls to work with
		t.join();
	}

}
