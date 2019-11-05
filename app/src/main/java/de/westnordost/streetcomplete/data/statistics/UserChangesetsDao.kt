package de.westnordost.streetcomplete.data.statistics

import java.util.Date

import javax.inject.Inject

import de.westnordost.osmapi.changesets.ChangesetInfo
import de.westnordost.osmapi.changesets.ChangesetsDao
import de.westnordost.osmapi.changesets.QueryChangesetsFilters
import de.westnordost.osmapi.common.Handler

/** Gets ALL changesets of a certain user, ordered by date. (OSM server limits the result set of one
 * single query to 100)  */
class UserChangesetsDao @Inject constructor(private val changesetsDao: ChangesetsDao) {

    fun findAll(handler: Handler<ChangesetInfo>, userId: Long, closedAfter: Date) {
        val relay = RememberLastHandlerRelay(handler)
        do {
            val filters = QueryChangesetsFilters().byUser(userId)
            relay.earliest?.let {
                filters.byOpenSomeTimeBetween(it.dateCreated, closedAfter)
            }
            relay.foundMore = false
            changesetsDao.find(relay, filters)
        } while (relay.foundMore)
    }

    private class RememberLastHandlerRelay(private val relayTo: Handler<ChangesetInfo>
    ) : Handler<ChangesetInfo> {
        var earliest: ChangesetInfo? = null
        var foundMore: Boolean = false

        override fun handle(info: ChangesetInfo) {
            if (earliest == null || earliest!!.dateCreated.after(info.dateCreated)) {
                earliest = info
                relayTo.handle(info)
                foundMore = true
            }
        }
    }
}
