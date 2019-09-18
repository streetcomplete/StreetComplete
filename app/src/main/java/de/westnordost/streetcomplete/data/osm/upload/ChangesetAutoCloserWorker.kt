package de.westnordost.streetcomplete.data.osm.upload

import android.content.Context

import javax.inject.Inject

import androidx.work.Worker
import androidx.work.WorkerParameters
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.common.errors.OsmConnectionException
import de.westnordost.streetcomplete.Injector

class ChangesetAutoCloserWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @Inject internal lateinit var openQuestChangesetsManager: OpenQuestChangesetsManager

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun doWork(): Result {
        try {
            openQuestChangesetsManager.closeOldChangesets()
        } catch (e: OsmConnectionException) {
            // wasn't able to connect to the server (i.e. connection timeout). Oh well, then,
            // never mind. Could also retry later with Result.retry() but the OSM API closes open
            // changesets after 1 hour anyway.
        } catch (e: OsmAuthorizationException) {
            // the user may not be authorized yet (or not be authorized anymore) #283
            // nothing we can do about here. He will have to reauthenticate when he next opens the app
            return Result.failure()
        }
        return Result.success()
    }
}
