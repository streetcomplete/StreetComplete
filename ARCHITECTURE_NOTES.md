# StreetComplete — Architecture Notes

Notes for a researcher reading the code, not a contributor guide. Written against this
working copy: **StreetComplete 63.4** (`app/build.gradle.kts:12`), which is a **Kotlin
Multiplatform** version of the app (`app/src/commonMain`, `app/src/androidMain`,
`app/src/iosMain`), with the UI written in Compose Multiplatform. This matters: older
StreetComplete write-ups describe an Android-only codebase with XML layouts and
`AbstractQuestAnswerFragment`; that structure no longer exists here. The iOS target is
present but incomplete (see the note on upload below).

Throughout, paths are relative to the repo root. Two prefixes recur so often that I
abbreviate them:

- `common/` = `app/src/commonMain/kotlin/de/westnordost/streetcomplete/`
- `android/` = `app/src/androidMain/kotlin/de/westnordost/streetcomplete/`

---

## 1. Where quest types are defined and registered

### The interfaces

- `common/data/quest/QuestType.kt` — `interface QuestType : EditType`. Very thin: adds
  `hint`, `hintImages`, `deleteMetadataOlderThan()`.
- `common/data/osm/edits/EditType.kt` — `interface EditType`. This is where a quest's
  identity actually lives: `icon`, `title`, `name` (defaults to
  `this::class.simpleName!!`), `wikiLink`, `achievements`, `defaultDisabledMessage`,
  `visibilityEditable`. The `name` is the string recorded as
  `StreetComplete:quest_type=<name>` in the changeset and used as the DB key everywhere.
- `common/data/osm/edits/ElementEditType.kt` — adds `changesetComment`.
- `common/data/osm/osmquests/OsmElementQuestType.kt` — `interface OsmElementQuestType<T> :
  QuestType, ElementEditType`. This is the interface nearly every quest implements. Its
  members are the heart of the quest contract:
  - `getApplicableElements(mapData): Iterable<Element>` — filter downloaded data down to
    elements that should get a quest.
  - `isApplicableTo(element): Boolean?` — re-check a single element. Returning `null`
    means "I need surrounding data to decide" (see §6).
  - `enabledInCountries`, `hasMarkersAtEnds`, `getHighlightedElements()`,
    `highlightedElementsRadius`.
  - `@Composable fun Form(on: (QuestAction<T>) -> Unit, element, geometry, countryInfo)` —
    the answer UI.
  - `applyAnswerTo(answer: T, tags: Tags, geometry, timestampEdited)` — turn the answer
    into tag changes.
- `common/data/osm/osmquests/OsmFilterQuestType.kt` — abstract class for the common case.
  You supply `elementFilter: String` (a filter-expression DSL, §7) and it implements
  `getApplicableElements` and `isApplicableTo` for you via
  `elementFilter.toElementFilterExpression()`.

`QuestAction<T>` is also declared at the bottom of `OsmElementQuestType.kt`: either
`Answer<T>(value)` or one of the `Action` enum constants (`Dismiss`, `CantSay`,
`LeaveNote`, `HideQuest`, `SplitWay`, `MoveNode`, `DeletePoi`, `ReplacePoi`).

### Individual quests

One package per quest under `common/quests/`, e.g. `common/quests/bus_stop_shelter/`,
`common/quests/surface/`, `common/quests/existence/`. A simple quest is a single file. Two
good reference implementations:

- `common/quests/bus_stop_shelter/AddBusStopShelter.kt` — ~70 lines: an `elementFilter`
  string, metadata (`changesetComment`, `wikiLink`, `icon`, `title`, `achievements`), a
  `Form` built from `QuestForm` + `AnswerItem`s, and `applyAnswerTo` writing
  `tags.updateWithCheckDate("shelter", "yes"/"no")`.
- `common/quests/surface/AddRoadSurface.kt` — same shape, uses the generic
  `ItemSelectQuestForm` and delegates tagging to `Surface.applyTo(tags)` in
  `common/osm/surface/SurfaceCreator.kt`.

Generic reusable forms live in `common/ui/common/quest/` — `QuestForm`,
`ItemSelectQuestForm`, `ItemsSelectQuestForm`, `GroupedItemSelectQuestForm`,
`RadioGroupQuestForm`, `CheckboxGroupQuestForm`, `YesNoQuestForm`, `CountInputQuestForm`,
`LocalizedNameQuestForm`, `NameWithSuggestionsQuestForm`. The list is documented in the
KDoc of `OsmElementQuestType.Form`.

### Registration

- `common/quests/QuestTypesRegistry.kt` (556 lines) — the single master list. The function
  `questTypeRegistry(arSupportChecker, getCountryInfoByLocation, getCountryOrSubdivisionCode, getFeature)`
  returns `QuestTypeRegistry(listOf(1 to AddX(), 2 to AddY(), …))`. Two things are encoded
  in this list at once:
  - **the integer ordinal**, which is a stable serialization id (never reused, never
    reordered);
  - **the list order**, which is the default display/priority order of quests. The file is
    heavily commented with the rationale for the ordering (e.g. `81 to AddOpeningHours()`,
    `134 to AddSidewalk(), // for any pedestrian routers, needs minimal thinking`).
- `common/data/quest/QuestTypeRegistry.kt` — `class QuestTypeRegistry(ordinalsAndEntries) :
  ObjectTypeRegistry<QuestType>`.
- `common/data/ObjectTypeRegistry.kt` — the generic machinery: `getByName(typeName)`,
  `getByOrdinal(ordinal)`, `getOrdinalOf(type)`, plus `AbstractList` iteration in
  declaration order. Its `init` block enforces that names and ordinals are unique. The
  KDoc says reflection was avoided because "that doesn't really work on Android".
- `common/CommonModule.kt:389-399` — the Koin DI binding: `single<QuestTypeRegistry> { … questTypeRegistry(...) }`,
  injecting `CountryInfos`, lazy `CountryBoundaries` and lazy `FeatureDictionary`.
- `common/data/AllEditTypes.kt` — unions the quest registry with the overlay registry so
  that a stored `quest_type` string can be resolved back to an `EditType` regardless of
  which kind it is. Used by `ElementEditsDao`.

Overlays (`common/overlays/`) are a parallel concept with their own registry; they produce
the same `ElementEditAction`s and share the whole downstream pipeline.

---

## 2. Full path from tapping a form to an OSM tag

Following an "Add bus stop shelter → yes" tap end to end:

1. **Pin tap.** `android/screens/main/map/components/PinsMapComponent.kt` →
   `onClick(position)` does `map.queryRenderedFeatures(...)` on `"pins-layer"` /
   `"pin-cluster-layer"` and calls back `onClickPin(properties)`.
2. `android/screens/main/map/MainMapFragment.kt:307` `onClickPin()` →
   `questPinsManager?.getQuestKey(properties)` → `listener?.onClickedQuest(questKey)`.
   `getQuestKey` decodes the GeoJSON feature properties (`element_type`, `element_id`,
   `quest_type`) back into an `OsmQuestKey` — see the private `Map<String,String>.toQuestKey()`
   at the bottom of `QuestPinsManager.kt`.
3. `android/screens/main/MainActivity.kt:461` `onClickedQuest()` →
   `mainBottomSheetViewModel.showQuest(questKey)`.
4. `common/screens/main/MainBottomSheetViewModel.kt:118` `showQuest()` → `showOsmQuest()`
   (line 172), which loads the `Element` from `MapDataWithEditsSource` and the `OsmQuest`
   from `OsmQuestSource`, then sets `shownBottomSheet.value = ShownBottomSheet.OsmQuest(quest, element)`.
5. `common/screens/main/bottom_sheet/MainBottomSheet.kt:94` renders
   `OsmQuestFormContainer(...)` for that state.
6. `common/screens/main/bottom_sheet/quest/OsmQuestFormContainer.kt:136` calls
   `questType.Form(on = ::onAction, element, geometry, countryInfo)` — i.e. the quest's own
   composable. The user taps "yes"; the form invokes `on(Answer(SHELTER))`.
7. **The key conversion step**, `OsmQuestFormContainer.onAction()` (same file, ~line 104):

   ```kotlin
   is Answer<T> -> {
       val changesBuilder = StringMapChangesBuilder(element.tags)
       questType.applyAnswerTo(action.value, changesBuilder, geometry, element.timestampEdited)
       val changes = changesBuilder.create()
       onEdit(UpdateElementTagsAction(element, changes))
   }
   ```

   `Tags` is just a typealias for `StringMapChangesBuilder`
   (`common/osm/TagsUtils.kt:5`). So `applyAnswerTo` never mutates an element — it writes
   into a diff builder. `common/data/osm/edits/update_tags/StringMapChangesBuilder.kt`
   records each `set`/`remove` as a `StringMapEntryAdd` / `StringMapEntryModify` /
   `StringMapEntryDelete`, and `create()` freezes them into an immutable
   `StringMapChanges` (`.../StringMapChanges.kt`). Storing a **diff** rather than a final
   tag map is what makes later conflict handling possible.
8. Back in `MainBottomSheet.kt:96`, `onEdit` checks `viewModel.isSurvey(geometry)`
   (`SurveyChecker`, compares against recent GPS track — see
   `ApplicationConstants.MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80.0` m and
   `MAX_RECENT_LOCATIONS_AGE = 10.minutes`). If not near, it shows a confirmation dialog
   first; otherwise it calls `viewModel.submitEdit(...)` directly.
9. `common/screens/main/MainBottomSheetViewModel.kt:140` `submitEdit()` →
   `elementEditsController.add(elementEditType, geometry, "survey", elementEditAction, isNearUserLocation)`.
10. `common/data/osm/edits/ElementEditsControllerImpl.kt:26` `add()` wraps it in an
    `ElementEdit(id=0, type, geometry, source, now, isSynced=false, action, isNearUserLocation)`
    and calls the private `add(edit)` (line ~130), which inside a lock does
    `editsDB.put(edit)`, `editElementsDB.put(...)`, `elementIdProviderDB.assign(...)`, then
    fires `onAddedEdit(edit)` to listeners.
11. **Local application (before any upload).**
    `common/data/osm/edits/MapDataWithEditsSourceImpl.kt:168` `elementEditsListener.onAddedEdit()`
    → `applyEdit(edit)` (line ~449) → `edit.action.createUpdates(this, idProvider)`.
    For our case that is `UpdateElementTagsAction.createUpdates()`
    (`common/data/osm/edits/update_tags/UpdateElementTagsAction.kt:44`), which fetches the
    current element, checks `isGeometrySubstantiallyDifferent`, and returns
    `MapDataChanges(modifications = listOf(currentElement.changesApplied(changes)))`.
    `changesApplied` is in `.../update_tags/StringMapChangesXt.kt:8` — it copies the tag
    map, applies the diff (`StringMapChanges.applyTo`, which throws if
    `hasConflictsTo(map)`), and returns a copy of the element with new tags and
    `timestampEdited = now`. The result is kept in in-memory overlay maps
    (`updatedElements`, `updatedGeometries`, `deletedElements`) — **not** written into the
    element tables.
12. That fires `callOnUpdated(...)`, which `OsmQuestController` listens to, causing the
    solved quest to disappear immediately (§6).
13. **Upload** (§5) eventually serializes the modified element into an `osmChange` XML
    document and POSTs it. `common/data/osm/mapdata/MapDataApiSerializer.kt` writes
    `<osmChange><modify><node id=… version=… changeset=…><tag k=… v=…/>…`.

So the tag string that lands in OSM is produced in exactly one place per quest —
`applyAnswerTo` — and everything after that is generic plumbing.

---

## 3. Where edits are stored locally before upload

SQLite. DB file name `streetcomplete_v2.db` (`common/ApplicationConstants.kt:13`); schema
version 20, created in `common/data/StreetCompleteDatabaseConfigurator.kt`
(`onCreate` lists every table; `onUpgrade` holds the migration history). The `Database`
abstraction is `common/data/Database.kt` / `DatabaseImpl.kt`; the Android binding is in
`android/AndroidModule.kt:75`.

The tables that matter for a pending element edit:

| Table | Defined in | Purpose |
|---|---|---|
| `osm_element_edits` | `common/data/osm/edits/ElementEditsTable.kt` | the edit queue itself |
| `osm_element_edits_elements` | `common/data/osm/edits/EditElementsTable.kt` | index: which edits touch which elements |
| `elements_by_edit` (ElementIdProvider) | `common/data/osm/edits/ElementIdProviderTable.kt` | pre-assigned negative ids for elements an edit will create |
| `osm_created_elements` | `common/data/osm/created_elements/` | elements created by the user |
| `osm_quests` | `common/data/osm/osmquests/OsmQuestTable.kt` | the open-quest index (not the edits) |
| `osm_quests_hidden` | `common/data/osm/osmquests/OsmQuestsHiddenTable.kt` | quests the user hid |
| `osm_note_edits` | `common/data/osmnotes/edits/NoteEditsTable.kt` | note creations/comments |
| `open_changesets` | `.../upload/changesets/OpenChangesetsTable.kt` | reusable open changeset ids |

`osm_element_edits` columns (`ElementEditsTable.Columns`): `id` (autoincrement),
`quest_type` (the `EditType.name` string), `geometry`, `source`, `latitude`, `longitude`,
`created`, `synced` (0/1), `action`, `is_near`.

Two columns are worth dwelling on:

- **`action`** is the whole `ElementEditAction` serialized to JSON.
  `common/data/osm/edits/ElementEditsDao.kt:39-57` builds a `Json` instance with a
  `polymorphic(ElementEditAction::class)` serializers module registering
  `UpdateElementTagsAction`, `RevertUpdateElementTagsAction`, `SplitWayAction`,
  `DeletePoiNodeAction`, `RevertDeletePoiNodeAction`, `CreateNodeAction`,
  `RevertCreateNodeAction`, `MoveNodeAction`, `RevertMoveNodeAction`,
  `CreateNodeFromVertexAction`. For a quest answer the serialized blob contains the
  *original element* and the *tag diff*.
- **`synced`** is the queue flag. `ElementEditsDao.getAllUnsynced()` /
  `getOldestUnsynced()` filter on `synced = 0` ordered by `created`; `markSynced(id)` flips
  it to 1. Rows are not deleted on upload — they stay as undo/edit history until
  `deleteSyncedOlderThan(timestamp)` prunes them
  (`ElementEditsControllerImpl.kt:53`, driven by `ApplicationConstants.MAX_UNDO_HISTORY_AGE`
  = 12 h).

The controller/source split is the usual pattern here:
`ElementEditsController` (write) / `ElementEditsSource` (read + listener) implemented by
`ElementEditsControllerImpl`. Listeners get `onAddedEdit`, `onSyncedEdit`, `onDeletedEdits`.

Crucially, **local edits are not merged into the element tables**. The element tables hold
downloaded server state; pending edits are layered on top at read time by
`common/data/osm/edits/MapDataWithEditsSourceImpl.kt`, which keeps `updatedElements`,
`updatedGeometries` and `deletedElements` maps in memory and rebuilds them from the
unsynced-edit queue in `rebuildLocalChanges()` (line 439) at construction and whenever
edits are deleted. Every consumer that should "see" the user's own unsynced work reads
through `MapDataWithEditsSource` rather than `MapDataSource`.

Undo: `common/data/edithistory/EditHistoryController.kt:86` `undo(editKey)` →
`ElementEditsController.undo(edit)` (`ElementEditsControllerImpl.kt:105`). If not yet
synced it just deletes the row; if already synced it creates a reverse action via
`IsActionRevertable.createReverted(...)` and enqueues that as a new edit.

---

## 4. How a solved quest disappears from the map immediately

Worth separating from §6, because it happens before any upload:

`ElementEditsControllerImpl.onAddedEdit` → `MapDataWithEditsSourceImpl.elementEditsListener.onAddedEdit`
(line 168) applies the edit to the in-memory overlay and calls `callOnUpdated(updated, deleted)`
→ `OsmQuestController.mapDataSourceListener.onUpdated` (line 62 of
`common/data/osm/osmquests/OsmQuestController.kt`) → re-evaluates all quest types against
the *edited* element → the shelter quest's filter no longer matches → the quest key ends up
in `obsoleteQuestKeys` → `db.deleteAll(obsoleteQuestKeys)` → `onUpdated(deleted = …)` →
`VisibleQuestsSource` → `QuestPinsManager.updateQuestPins(added, removed)` → pin removed.

---

## 5. Where and how upload to OSM happens

### Triggering

- Interface: `common/data/upload/UploadController.kt` (`fun upload(isUserInitiated: Boolean)`).
- Android impl: `android/data/upload/AndroidUploadController.kt` — enqueues a unique
  WorkManager job (`UploadWorker`, same file) with `ExistingWorkPolicy.KEEP`; the worker
  runs as a foreground service with a sync notification and simply calls `uploader.upload()`.
- iOS impl: `app/src/iosMain/.../data/upload/IosUploadController.kt` — `TODO("Not yet implemented")`.
  **Upload is Android-only in this tree.**
- Auto-trigger: `android/data/quest/QuestAutoSyncer.kt` — a `DefaultLifecycleObserver` that
  calls `triggerAutoUpload()` when unsynced-change count increases, when the user logs in,
  and on network availability, honouring the `Autosync` preference (`ON` / `WIFI` / `OFF`).
- Manual trigger: `common/screens/main/MainViewModelImpl.kt` injects `UploadController`.

### The upload sequence

`common/data/upload/Uploader.kt` — `suspend fun upload()`:
1. version-ban check (`VersionIsBannedChecker`), throws `VersionBannedException`;
2. `if (!userLoginSource.isLoggedIn) throw AuthorizationException(...)`;
3. under a mutex, `elementEditsUploader.upload()` **then** `noteEditsUploader.upload()` —
   in that order, deliberately, because notes may need element ids that the element upload
   just created;
4. on `AuthorizationException` it logs the user out; on `ConflictException` handled
   downstream it calls `invalidateArea(pos)`, invalidating the downloaded-tile record so
   the area is re-downloaded.

`common/data/osm/edits/upload/ElementEditsUploader.kt`:
- `upload()` loops `elementEditsController.getOldestUnsynced()` until empty, uploading one
  edit at a time inside `withContext(NonCancellable)` (comment explains: cancelling
  mid-request could double-upload or lose the "star").
- `uploadEdit()` on success: `elementEditsController.markSynced(edit, updates)`,
  `mapDataController.updateAll(updates)`, `noteEditsController.updateElementIds(...)`, and
  `statisticsController.addOne(edit.type.name, edit.position)` (or `subtractOne` for a
  revert).
- on `ConflictException` or `IllegalArgumentException`: `markSyncFailed(edit)` (which just
  deletes the edit — see `ElementEditsControllerImpl.markSyncFailed`), fires `onDiscarded`,
  and re-fetches the current server version of the affected elements so the quest doesn't
  simply reappear as unsolved.

`common/data/osm/edits/upload/ElementEditUploader.kt` — `upload(edit, getIdProvider)`:
- Some actions are forced to use remote data
  (`ApplicationConstants.EDIT_ACTIONS_NOT_ALLOWED_TO_USE_LOCAL_CHANGES`, currently just
  `SplitWayAction`, because route relations aren't stored locally).
- Otherwise: build changes from the **local** cache first
  (`edit.action.createUpdates(mapDataSource, idProvider)`), upload; on
  `ChangesetTooLargeException` retry with a fresh changeset; on `ConflictException` fall
  back to `uploadUsingRemoteRepo()`, which rebuilds the change against
  `RemoteMapDataRepository(mapDataApi)` — a fresh fetch of the element from the API — and
  retries. `backfillMapDataUpdates()` then pulls in any way-member nodes not present
  locally.

### Changesets

`common/data/osm/edits/upload/changesets/OpenChangesetsManager.kt`:
- `getOrCreateChangeset(type, source, position, createNewIfTooFarAway)` reuses an open
  changeset keyed by `(quest type name, source)` from `open_changesets`, but opens a new one
  if the edit is more than `ApplicationConstants.CHANGESET_MAX_LAST_EDIT_DISTANCE` (5 km)
  from the last edit in it.
- `createChangesetTags()` sets: `comment` = `ElementEditType.changesetComment`,
  `created_by` = `"StreetComplete <version>"`, `locale`,
  `StreetComplete:quest_type` = the quest type `name`, and `source` (which is the string
  `"survey"` passed at `submitEdit`).
- `closeOldChangesets()` closes changesets after
  `CLOSE_CHANGESETS_AFTER_INACTIVITY_OF` = 20 min; `ChangesetAutoCloser` schedules it.

### The wire

`common/data/osm/mapdata/MapDataApiClientImpl.kt:28` `uploadChanges()`:
`POST {baseUrl}changeset/{id}/upload` via Ktor with `bearerAuth(accessToken)` and a body
built by `MapDataApiSerializer.serialize(changes, changesetId)` (osmChange XML,
`common/data/osm/mapdata/MapDataApiSerializer.kt`). HTTP 409/412/410/404 are all mapped to
`ConflictException`; 413 to `ChangesetTooLargeException`. The response is parsed by
`MapDataApiParser.parseElementUpdates` into `MapDataUpdates` (new ids and versions), which
is what flows back into `markSynced` and `MapDataController.updateAll`.

Base URL: `common/CommonModule.kt:219-221` —
`https://api.openstreetmap.org/api/0.6/`, or the dev API when
`ApplicationConstants.USE_TEST_API` is true (it is `false` here). OAuth2 constants in
`common/data/user/OAuthConstants.kt`.

---

## 6. How quest markers are rendered on the map

Rendering is **Android-only** in this tree: `common/screens/main/map/` contains only
`PresetIcons.kt` and `maplibre/Camera.kt`; the actual map is
`android/screens/main/map/`, built on MapLibre (`org.maplibre.android`). I did not find an
iOS map implementation.

Chain:

1. **Quest supply.** `common/data/quest/VisibleQuestsSource.kt` unions
   `OsmQuestSource` and `OsmNoteQuestSource` and filters out quests that are hidden
   (`QuestsHiddenSource`), whose type is disabled (`VisibleEditTypeSource`), filtered out by
   team mode (`TeamModeQuestFilterSource`), or superseded by the selected overlay
   (`SelectedOverlaySource`). It caches spatially (`SpatialCache`) and exposes
   `getAll(bbox)` plus a `Listener` with `onUpdated(added, removed)` / `onInvalidated()`.
2. **`android/screens/main/map/QuestPinsManager.kt`** is the glue.
   - `onNewScreenPosition()` → `updateCurrentScreenArea()`: requires zoom ≥ 14, converts the
     screen area to a `TilesRect` at `TILES_ZOOM = 16`, skips if > 32 tiles, and cancels a
     previous in-flight job (the long comment there explains the fast-panning problem).
   - `setQuestPins(bbox)` calls `visibleQuestsSource.getAll(bbox)` and builds pins.
   - `createQuestPins(quest)` → `quest.markerLocations.map { Pin(it, quest.type.icon.toAndroidResourceId()!!, props, order) }`.
     `order` comes from `questTypeOrders`, initialized in `initializeQuestTypeOrders()` from
     the `QuestTypeRegistry` order as re-sorted by `QuestTypeOrderSource` (user's custom
     ordering) — this becomes the symbol sort key, i.e. which pin wins a collision.
   - `props` come from `QuestKey.toProperties()` (bottom of the file): `quest_group` =
     `"osm"` / `"osm_note"`, plus `element_type` / `element_id` / `quest_type` or `note_id`.
     This is the round-trip that makes a tapped GeoJSON feature resolvable back to a quest.
   - `questsInView` is kept keyed by `QuestKey` and pruned carefully so that a long way's
     off-screen-centre quest doesn't lose its on-screen pins.
3. **Multiple pins per quest.** `common/data/osm/osmquests/OsmQuest.kt` — `markerLocations`
   is a lazy property: for an `ElementPolylinesGeometry` longer than a threshold it places
   `2 + length/MAXIMUM_MARKER_DISTANCE` markers along the polyline via
   `pointsOnPolylineFromStart(...)`, with `MAXIMUM_MARKER_DISTANCE = 400` m and
   `MARKER_FROM_END_DISTANCE = 15` m; if `questType.hasMarkersAtEnds` the threshold and
   placement favour the ends. Otherwise a single marker at `geometry.center`.
4. **`android/screens/main/map/components/PinsMapComponent.kt`** owns the MapLibre objects:
   a single clustering `GeoJsonSource("pins-source")` (`withCluster(true)`,
   `withClusterMaxZoom(14)`, `withClusterRadius(55)`) and three layers:
   - `"pin-cluster-layer"` (`SymbolLayer`) — the numbered cluster bubbles, zoom 13…14;
   - `"pin-dot-layer"` (`CircleLayer`) — the little white dot marking the exact position;
   - `"pins-layer"` (`SymbolLayer`) — the actual pins above zoom 14, `iconImage(get("icon-image"))`,
     `iconSize(1f)` (constant, with a comment that dynamic sizing caused flickering),
     `symbolSortKey(get("icon-order"))`.
   `set(pins)` registers the needed icon bitmaps via `MapImages.addOnce` +
   `createPinBitmap` (`android/screens/main/map/MapIconBitmapCreator.kt`, draws the pin
   shape + shadow + the quest icon into a 71dp bitmap), converts pins to a
   `FeatureCollection` and calls `pinsSource.setGeoJson(...)` on the main thread.
   `onClick` distinguishes a cluster (has `point_count` → `zoomToCluster`) from a pin
   (→ `onClickPin(properties)`).

Edit-history pins reuse the same component via
`android/screens/main/map/EditHistoryPinsManager.kt`.

---

## 7. How a quest is determined complete and removed

There is no "completed" flag on a quest anywhere. **A quest exists iff its quest type's
filter currently matches the element.** Answering it changes the element such that the
filter stops matching, and the quest is then deleted as obsolete. That's the whole
mechanism.

The controller is `common/data/osm/osmquests/OsmQuestController.kt`. Two entry points, both
listeners on `MapDataWithEditsSource`:

- `mapDataSourceListener.onUpdated(updated, deleted)` (line 62) — fired when elements
  change (a local edit was added, an upload came back, an element was deleted). For each
  updated element it runs `createQuestsForElementDeferred(element, geometry, allQuestTypes)`,
  which for each quest type calls `questType.isApplicableTo(element)`; if that returns
  `null` it falls back to fetching a padded bounding box
  (`ApplicationConstants.QUEST_FILTER_PADDING` = 20 m) from the DB and running
  `getApplicableElements` over it. Then:
  ```kotlin
  val previousQuests = db.getAllForElements(updated.map { it.key })
  val deleteQuestKeys = db.getAllForElements(deleted).map { it.key }
  obsoleteQuestKeys = getObsoleteQuestKeys(quests, previousQuests, deleteQuestKeys)
  updateQuests(quests, obsoleteQuestKeys)
  ```
  `getObsoleteQuestKeys()` (line 212) is literally set subtraction: quests that existed
  before for these elements and are not in the freshly computed set, plus everything
  attached to deleted elements. `updateQuests()` (line 227) does
  `db.deleteAll(obsoleteQuestKeys)` + `db.putAll(questsNow)`. Then
  `onUpdated(added = visibleQuests, deleted = obsoleteQuestKeys)` propagates to
  `VisibleQuestsSource` → `QuestPinsManager`.
- `mapDataSourceListener.onReplacedForBBox(bbox, mapData)` (line 96) — fired after a
  download (`MapDataDownloader.download()` → `MapDataController.putAllForBBox()` →
  `onReplacedForBBox`, `common/data/osm/mapdata/MapDataControllerImpl.kt:44,269`). Runs
  `createQuestsForBBox`, which parallelizes across quest types
  (`scope.async` per type), skips types disabled in the country
  (`countryBoundaries.intersects(bbox, questType.enabledInCountries)`), and short-circuits
  tagless elements for `OsmFilterQuestType`s whose filter can't match without tags
  (`filter.mayEvaluateToTrueWithNoTags` — the comment claims ~15–30 % speedup). Then the
  same obsolete-key diffing against `db.getAllInBBox(bbox)`.

Other ways a quest goes away:

- **Hidden by the user** (`Action.HideQuest` / "can't say" → hide):
  `MainBottomSheetViewModel.hideQuest()` → `QuestsHiddenController.hide(key)`
  (`common/data/visiblequests/QuestsHiddenControllerImpl.kt:36`), stored in
  `osm_quests_hidden` with a timestamp. `VisibleQuestsSource` filters these out; they are
  un-hideable from the edit history.
- **A note sits on the same spot.** `OsmQuestController` treats note positions as a
  blacklist: `getBlacklistedPositions(bbox)` / `isBlacklistedPosition(pos)`, compared at
  6-decimal precision (`truncateTo6Decimals`). Solving a quest by leaving a note therefore
  suppresses quests there.
- **Upload conflict.** `ElementEditsUploader.uploadEdit()`'s `ConflictException` branch
  deletes the edit and re-fetches server state, which will re-create the quest if it still
  applies.
- **Persistence storage:** `osm_quests` (`OsmQuestTable`) is a plain index —
  `(element_type, element_id, quest_type)` primary key plus lat/lon and a spatial index. No
  status column exists.
- **Old data cleanup:** `common/data/Cleaner.kt` deletes map data / notes / tiles older than
  `DELETE_OLD_DATA_AFTER` (14 days) and calls
  `questTypeRegistry.forEach { it.deleteMetadataOlderThan(...) }`.

---

## 8. Resurvey and the `check_date` mechanism

Two halves: **asking again** (filter side) and **recording the answer** (tag side).

### Recording: `common/osm/ResurveyUtils.kt`

- `const val SURVEY_MARK_KEY = "check_date"` — the key StreetComplete itself writes.
- `LAST_CHECK_DATE_KEYS = ["check_date", "lastcheck", "last_checked", "survey:date", "survey_date"]`
  — whole-element check-date keys it *reads*.
- `getLastCheckDateKeys(key)` returns the per-tag variants it reads:
  `"$key:check_date"`, `"check_date:$key"`, `"$key:lastcheck"`, `"lastcheck:$key"`,
  `"$key:last_checked"`, `"last_checked:$key"`.
- `Tags.updateWithCheckDate(key, value)` — the workhorse called by most quests:
  ```kotlin
  val previousValue = get(key)
  set(key, value)
  if (previousValue == value || hasCheckDateForKey(key) || hasCheckDate()) {
      updateCheckDateForKey(key)
  }
  ```
  I.e. if the answer **confirms** the existing value, stamp the check date; if the answer
  **changes** the value, only stamp a check date if one was already present. The comment
  explains this changed in v32.0 — previously a changed value deleted the check date, which
  destroyed another surveyor's data.
- `Tags.setCheckDateForKey(key, date)` first calls `removeCheckDatesForKey(key)` (clearing
  the less-preferred spellings) and then writes `check_date:$key=YYYY-MM-DD`; it also
  refreshes the whole-element `check_date` if one exists, "to avoid ambiguities".
- `Tags.updateCheckDate()` / `setCheckDate(date)` do the whole-element equivalent, writing
  `check_date=YYYY-MM-DD` and removing the other `LAST_CHECK_DATE_KEYS`.
- `String.toCheckDate()` parses `YYYY-MM-DD` or `YYYY-MM` via
  `OSM_CHECK_DATE_REGEX`; `nowAsCheckDateString()` produces today's.

Quests that are *purely* resurvey quests call `tags.updateCheckDate()` with no value
change, e.g. `common/quests/existence/CheckExistence.kt:136`,
`common/quests/shop_type/CheckShopExistence.kt:77`,
`common/quests/shop_type/CheckShopType.kt:73`.

### Asking again: the `older` / `newer` filter operators

The `elementFilter` DSL is parsed by `common/data/elementfilter/ElementFiltersParser.kt`.
Relevant grammar:

- `older <date>` / `newer <date>` with no key → `ElementOlderThan` / `ElementNewerThan`
  (parser lines 225-232).
- `<key> older <date>` → `CombineFilters(HasKey(key), TagOlderThan(key, date))`
  (line 238).
- `<date>` is either a literal `YYYY-MM-DD` (`FixedDate`) or `today ± N years|months|weeks|days`
  (`parseDateFilter`, line 341; `parseDurationInDays`, line 367 — a year is 365.25 days, a
  month 30.5 days).

The semantics are in `common/data/elementfilter/filters/ElementFilter.kt`:

```kotlin
abstract class CompareTagAge(val key: String, val dateFilter: DateFilter) : ElementFilter {
    override fun matches(obj: Element): Boolean {
        if (compareTo(Instant.fromEpochMilliseconds(obj.timestampEdited).toLocalDate())) return true
        return getLastCheckDateKeys(key)
            .mapNotNull { obj.tags[it]?.toCheckDate() }
            .any { compareTo(it) }
    }
}
```

So `surface older today -6 years` matches if **either** the element's last edit timestamp is
older than 6 years **or** any recognised per-key check-date tag is older than 6 years.
`CompareElementAge` (for the keyless `older`) uses only `timestampEdited`.
There are also plain value comparisons on date-valued tags — `HasDateTagLessThan` etc.,
used e.g. by `CheckExistence.lastChecked()` which builds
`older today -Nyears or check_date < today -N years or …`.

Concrete examples in this tree:
- `AddBusStopShelter.elementFilter`: `and (!shelter or shelter older today -4 years)`.
- `AddRoadSurface.elementFilter`: unpaved surfaces re-asked after 6 years, everything else
  after 12; it also excludes `paved`/`unpaved` generics that carry
  `surface:note`/`note:surface`/`check_date:surface`.
- `CheckExistence` has a whole tiered table — ATMs and vending machines 2 years, benches and
  waste baskets 6, bicycle parking 10, traffic calming 14.

### The user-facing multiplier

`common/data/elementfilter/filters/RelativeDate.kt`:

```kotlin
class RelativeDate(val deltaDays: Float) : DateFilter {
    override val date: LocalDate get() { … deltaDays * MULTIPLIER * 24 hours … }
    companion object { var MULTIPLIER: Float = 1f }
}
```

A global mutable multiplier applied to every relative date in every filter.
`common/data/preferences/ResurveyIntervals.kt` defines
`LESS_OFTEN(2.0f) / DEFAULT(1.0f) / MORE_OFTEN(0.5f)` and `ResurveyIntervalsUpdater` keeps
`RelativeDate.MULTIPLIER` in sync with the preference (`quests.resurveyIntervals`,
`common/data/preferences/Preferences.kt:249`), both at startup and via a settings listener.
The setting is surfaced in `common/screens/settings/SettingsScreen.kt:131`.

Note the ordering subtlety: because `MULTIPLIER` is read inside the `date` getter (not at
filter-construction time), changing the preference takes effect on the next filter
evaluation. Whether existing already-created quests are re-evaluated promptly after a
change depends on whether something invalidates the quest DB — I did not trace a listener
that forces re-creation of quests on a resurvey-interval change, so I'm not certain the
change is visible before the next download or element update.

---

## Things I could not confirm / open questions

- **iOS.** `IosUploadController.upload()` is `TODO("Not yet implemented")` and there is no
  iOS map/pins code under `app/src/iosMain`. I read the KMP structure as work in progress;
  everything in §5 (upload triggering) and §6 (rendering) describes the Android target only.
  The data layer in `commonMain` is genuinely shared.
- **Resurvey interval change propagation** — see the last paragraph of §8.
- I did not trace the **note quest** (`OsmNoteQuest`) pipeline in comparable depth; it runs
  through `common/data/osmnotes/` with its own controller, DAO (`note_edits`) and uploader
  (`NoteEditsUploader`), and joins the shared path at `VisibleQuestsSource` and
  `PinsMapComponent`.
- I did not verify runtime behaviour — nothing here was built or executed; this is a
  reading of the source only.
- `CONTRIBUTING_A_NEW_QUEST.md`, `QUEST_GUIDELINES.md` and `OVERLAY_GUIDELINES.md` in the
  repo root are the project's own prose on quest design and are worth reading alongside
  this; I used them only for orientation and have not cross-checked every claim in them
  against the current code.
