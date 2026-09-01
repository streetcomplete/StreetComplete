package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AnyThread
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.AutoSyncer
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

/** Android host for the shared Compose Multiplatform main screen. */
class MainActivity :
    BaseActivity(),
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val autoSyncer: AutoSyncer by inject()
    private val preferences: Preferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val periodicCleaner: PeriodicCleaner by inject()

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handleIntent(intent)

        lifecycle.addObserver(autoSyncer)

        feedsUpdater.updateAtMostDaily()
        // this must be enqueued once the UI is started, i.e. not in headless mode. This is why
        // it is done here, rather than in AppInitializer. Reason is that
        // AppInitializer.initialize() is also executed when a background job is run. But we don't
        // want to enqueue the cleanup job again while running the cleanup job, but only once after
        // the user actually opened the app!
        periodicCleaner.enqueue()

        val composeView = ComposeView(this)
        setContentView(composeView)
        composeView.setContent {
            AppTheme {
                val context = LocalContext.current
                MainScreen(
                    viewModel = viewModel,
                    editHistoryViewModel = editHistoryViewModel,
                    mainBottomSheetViewModel = mainBottomSheetViewModel,
                    onClickSettings = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    onClickQuestSettings = {
                        context.startActivity(
                            SettingsActivity.createLaunchQuestSettingsIntent(context)
                        )
                    },
                    onClickAbout = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    },
                    onClickProfile = {
                        context.startActivity(Intent(context, UserActivity::class.java))
                    },
                    onClickLogin = {
                        context.startActivity(
                            Intent(context, UserActivity::class.java).apply {
                                putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
                            }
                        )
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateScreenOn()
        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        visibleQuestsSource.removeListener(this)
        mapDataWithEditsSource.removeListener(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        intent.data?.toString()?.let(viewModel::setUri)
    }

    private fun updateScreenOn() {
        if (preferences.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @AnyThread
    override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val openQuestKey = when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
            is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
            is ShownBottomSheet.OsmQuest -> shown.quest.key
            else -> return
        }
        if (openQuestKey in removed) mainBottomSheetViewModel.closeBottomSheet()
    }

    @AnyThread
    override fun onInvalidated() {
        val openQuestKey = when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
            is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
            is ShownBottomSheet.OsmQuest -> shown.quest.key
            else -> return
        }
        lifecycleScope.launch {
            val openQuest = withContext(Dispatchers.IO) {
                visibleQuestsSource.get(openQuestKey)
            }
            if (openQuest == null) mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val openElementKey =
            (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)
                ?.element?.key ?: return
        if (openElementKey in deleted) mainBottomSheetViewModel.closeBottomSheet()
    }

    @AnyThread
    override fun onReplacedForBBox(
        bbox: BoundingBox,
        mapDataWithGeometry: MapDataWithGeometry,
    ) {
        val openElementKey =
            (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)
                ?.element?.key ?: return
        lifecycleScope.launch {
            val openElement = withContext(Dispatchers.IO) {
                mapDataWithEditsSource.get(openElementKey.type, openElementKey.id)
            }
            if (openElement == null) mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onCleared() {
        if (mainBottomSheetViewModel.shownBottomSheet.value is ShownBottomSheet.Overlay) {
            mainBottomSheetViewModel.closeBottomSheet()
        }
    }
}
