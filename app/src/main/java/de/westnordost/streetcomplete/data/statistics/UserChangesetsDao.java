package de.westnordost.streetcomplete.data.statistics;

import java.util.Date;

import javax.inject.Inject;

import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.changesets.QueryChangesetsFilters;
import de.westnordost.osmapi.common.Handler;

/** Gets ALL changesets of a certain user, ordered by date. (OSM server limits the result set of one
 *  single query to 100) */
public class UserChangesetsDao
{
	private final ChangesetsDao changesetsDao;

	@Inject public UserChangesetsDao(ChangesetsDao changesetsDao)
	{
		this.changesetsDao = changesetsDao;
	}

	public void findAll(final Handler<ChangesetInfo> handler, long userId, Date closedAfter)
	{
		RememberLastHandlerRelay relay = new RememberLastHandlerRelay();
		relay.relayTo = handler;
		do
		{
			QueryChangesetsFilters filters = new QueryChangesetsFilters().byUser(userId);
			if(relay.earliest != null)
			{
				filters.byOpenSomeTimeBetween(relay.earliest.dateCreated, closedAfter);
			}

			relay.foundMore = false;
			changesetsDao.find(relay, filters);
		} while ( relay.foundMore );
	}


	private static class RememberLastHandlerRelay implements Handler<ChangesetInfo>
	{
		Handler<ChangesetInfo> relayTo;
		ChangesetInfo earliest;
		boolean foundMore;

		@Override public void handle(ChangesetInfo tea)
		{
			if(earliest == null || earliest.dateCreated.after(tea.dateCreated))
			{
				earliest = tea;
				relayTo.handle(tea);
				foundMore = true;
			}
		}
	}
}
