package de.westnordost.streetcomplete.data.osm.download;

import junit.framework.TestCase;

import javax.inject.Provider;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.OsmConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OverpassMapDataDaoTest extends TestCase
{
	public void testHandleOverpassQuota() throws InterruptedException
	{
		Provider provider = mock(Provider.class);
		when(provider.get()).thenReturn(mock(OverpassMapDataParser.class));

		OverpassStatus status = new OverpassStatus();
		status.availableSlots = 0;
		status.nextAvailableSlotIn = 2;

		OsmConnection osm = mock(OsmConnection.class);
		when(osm.makeRequest(eq("status"), any(OverpassStatusParser.class))).thenReturn(status);
		when(osm.makeRequest(eq("interpreter"), eq("POST"), eq(false), any(ApiRequestWriter.class), any(OverpassStatusParser.class)))
				.thenThrow(OsmTooManyRequestsException.class);

		final OverpassMapDataDao dao = new OverpassMapDataDao(osm, provider);

		// the dao will call get(), get an exception in return, ask its status
		// then and at least wait for the specified amount of time before calling again
		final boolean[] result = new boolean[1];
		Thread dlThread = new Thread()
		{
			@Override public void run()
			{
				// assert false because we interrupt the thread further down...
				result[0] = dao.getAndHandleQuota("", null);
			}
		};
		dlThread.start();

		// sleep the wait time: Downloader should not try to call
		// overpass again in this time
		Thread.sleep(status.nextAvailableSlotIn * 1000);
		verify(osm, times(1)).makeRequest(eq("interpreter"), eq("POST"), eq(false), any(ApiRequestWriter.class), any(OverpassStatusParser.class));
		verify(osm, times(1)).makeRequest(eq("status"), any(OverpassStatusParser.class));

		// now we test if dao will call overpass again after that time. It is not really
		// defined when the downloader must call overpass again, lets assume 1.5 secs here and
		// change it when it fails
		Thread.sleep(1500);
		verify(osm, times(2)).makeRequest(eq("interpreter"), eq("POST"), eq(false), any(ApiRequestWriter.class), any(OverpassStatusParser.class));
		verify(osm, times(2)).makeRequest(eq("status"), any(OverpassStatusParser.class));

		// we are done here, interrupt thread (still part of the test though...)
		dlThread.interrupt();
		dlThread.join();
		assertFalse(result[0]);
	}
}
