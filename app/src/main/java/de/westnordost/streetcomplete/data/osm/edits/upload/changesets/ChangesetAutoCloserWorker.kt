package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.content.Context

import javax.inject.Inject

import androidx.work.Worker
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.user.AuthorizationException

class ChangesetAutoCloserWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @Inject internal lateinit var openQuestChangesetsManager: OpenQuestChangesetsManager

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun doWork(): Result {
        try {
            openQuestChangesetsManager.closeOldChangesets()
        } catch (e: ConnectionException) {
            // wasn't able to connect to the server (i.e. connection timeout). Oh well, then,
            // never mind. Could also retry later with Result.retry() but the OSM API closes open
            // changesets after 1 hour anyway.
        } catch (e: AuthorizationException) {
            // the user may not be authorized yet (or not be authorized anymore) #283
            // nothing we can do about here. He will have to reauthenticate when he next opens the app
            return Result.failure()
        }
        return Result.success()
    }
}
