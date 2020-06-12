package de.westnordost.streetcomplete.data.download

import dagger.Module
import dagger.Provides

@Module
object DownloadModule {
    @Provides
    fun downloadProgressSource(downloadController: QuestDownloadController): QuestDownloadProgressSource =
        downloadController
}