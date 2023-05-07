SCEE is a modified version of StreetComplete, aimed at experienced OSM users unhappy about the lack of advanced editing capabilities in normal StreetComplete.
By default, most of the additional capabilities are disabled. Go through the settings (either in the app or [below](#differences-to-streetcomplete)) for details.

Please be aware that SCEE is not suitable for people used to discard warning messages without reading!
Users new to OpenStreetMap are best advised to use StreetComplete.

Functionality added in SCEE is considerably less tested than what you might be used from StreetComplete, so bugs or unexpected behavior may happen. If you encounter any, please report the issue.

Due to the different name used in changesets ("StreetComplete_ee"), edits made with this version do not contribute to the displayed StreetComplete statistics and star count.

1. [Download](#download-scee)
2. [Translate](#translations)
3. [Additional permissions](#permissions)
4. [Differences to StreetComplete](#differences-to-streetcomplete)
5. [Contributing quests](#contributing-quests)
6. [Differences in changesets](#changeset-differences-compared-to-streetcomplete)
7. [StreetComplete readme](#original-streetcomplete-readme-below)

## Download SCEE

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/de.westnordost.streetcomplete.expert/)
[<img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Download APK from GitHub" height="80">](https://github.com/Helium314/SCEE/releases/latest)

F-Droid releases of SCEE make use of reproducible builds, so releases on F-Droid and GitHub are signed with the same keys. This means you can switch between GitHub and F-Droid releases anytime without needing to uninstall first.

## Translations
Translations for strings added in SCEE can be done [using Weblate](https://translate.codeberg.org/projects/scee/).

## Permissions
SCEE has more permissions than StreetComplete: `ACCESS_BACKGROUND_LOCATION` and `POST_NOTIFICATIONS`. Both are used only in an experimental feature to notify about nearby quests when the app is in background.

## Differences to StreetComplete
* Non-optional differences to StreetComplete
  * No statistics: To avoid unfair competition, a slightly changed name avoids changes being counted towards StreetComplete statistics, making the star count rather useless (thus it is not shown)
    * When using auto-upload, an indicator now shows when there are changes waiting to be uploaded
  * Dark theme uses dark buttons
  * Prevent short scroll to user location at app start when map was at a different position
  * Downloading data will interrupt upload queue (will resume afterwards)
  * Manual downloads can be queued instead if always cancelling the previous one
  * Show all hidden quests on long press on undo button (works only for downloaded areas)
  * Additional answers for some quests
    * Additional building types
    * Additional path surfaces
    * Specify that a crossing is raised
    * Answer non-marked lanes with a count
    * Answer "no seating, but not takeaway only"
    * Add wheelchair description when answering wheelchair quest
  * Move the "no cycleway" answer to more accessible position
  * Highlight obstacles along the way for smoothness quests
  * Open settings when pressing menu key in main menu dialog
  * Allow switching to aerial view while adding or moving a node
  * Some potential performance improvements
* New quests that are not eligible for StreetComplete, usually because some answers cannot be tagged, or because not everyone has the required knowledge to answer the quest
  * Material of benches and picnic tables
  * Phone number and website
  * Cuisine
  * Healthcare speciality
  * Outdoor seating type
  * Service building type
  * Service building operator
  * Artwork type
  * Railway platform number
  * Tree genus / species quest
    * Allows providing a file containing translated tree names instead of the default english ones
  * Quests based on external sources
    * Osmose quest showing Osmose issues as quests, with filter options
    * Custom quest from CSV file, allows creating nodes (see in-app description)
  * Show POI quests with the sole purpose of indicating existence of elements of chosen type (may show labels)
  * Option to show only quests added in SCEE in quest selection menu
  * Some "other answers" result in a modified changeset comment (because in SCEE they may contain more unexpected changes)
* Customizable overlays: Choose which elements are highlighted, and which tag is used to determine the color
* Settings
  * Additional darker dark theme
  * Background map can be changed to aerial / satellite imagery
  * Separate deletion of quest and map tile cache
  * Adjust location update intervals
  * Log reader (not a setting, but it's in the menu)
  * Expert mode that enables capabilities, some of which can be dangerous when used by inexperienced OSM contributors
    * Directly edit tags, with suggestions from iD and last used values
    * Add nodes everywhere, either free-floating or as part of a ways
      * inserting nodes into a way may actually re-use existing nodes at that position
    * Delete free-floating nodes
    * Additional "other answers"
      * add `access=private` to benches, bicycle parkings, picnic tables, pitches, (leisure) tracks and recycling containers
      * tag/adjust highway access
      * tag highways as under construction (with finish date)
      * tag buildings as demolished
      * add conditional maxspeed
    * Allow moving nodes that are part of a way (including a clear warning about changing geometry)
    * Allow disabling and moving the note quest
    * Allow closing notes
    * Some of the settings below can only be enabled in expert mode
  * Quest settings for most quests, mostly for customized element selection, but also for other things like allowing generic paved surface answer without note
    * Such customization should be handled with care. There are some safeguards, but modifying element selection could still lead to inappropriate tagging, quests being asked over and over again, and maybe app crashes.
  * UI settings
    * Quick settings button for switching preset, background and reverse quest order. Also contains a level filter for displayed quests / overlay elements
    * Quick selector for overlays
    * Show next quest for this element immediately
    * Show nearby quests / other quests for same element when quest form is open
    * Hide button for temporarily hiding quests (long press for permanent hide)
    * Zoom using volume buttons
    * Auto-select first edit when opening edit history
    * Search features in local language and all languages enabled in the system
    * Select how many lines the form needs to have to move recent selection to front
    * Show all main menu items as grid
    * Capitalize words when entering names
  * Display settings
    * Disable 3D buildings
    * Show arrows indicating direction of highlighted way
    * Highlight geometries for nearby quests
    * Put pin to exact location of a quest
    * Disable quest solved animation
    * Provide GPX track and have it always shown on the map
  * Quest settings
    * Hide or increase priority of quests depending on time of day
    * Force resurvey for specific tags
    * Different quest settings for each preset
    * Dynamic quest creation for immediately applying changed quest settings and resurvey intervals
    * Notifications about nearby quests when app is in background
  * Note settings
    * Create personal notes in a GPX file (adds a new button when creating a note)
    * Swap OSM and GPX note buttons, for switching default notes
    * Disable hiding the keyboard before creating a note
    * Create custom quests like notes
    * Save full-size photos made for notes
    * Hide notes created by specific users
  * Data management settings
    * Disable auto-download
    * Disable always downloading map data on manual download, even if data is fresh
    * Choose tile URL for aerial imagery
    * Set data retention time
    * Store map tiles on SD card
    * Import / export
      * Custom overlays
      * Quest presets, including per-preset quest settings
      * Hidden quests
      * All other settings, including quest settings and recently selected answers. Does not export login data.

Database and preferences files are compatible with StreetComplete, so if you have root privileges you can transfer them in either direction.

## Contributing quests
The original [contributing guidelines](#contributing) are still valid, but note that the [guidelines for contributing a quest](QUEST_GUIDELINES.md) have been significantly relaxed:
* Creating, moving and deleting nodes is possible
  * Inserting nodes into a way is not (yet) possible
* Guidelines are useful suggestions, but not enforced.
* Quests may be based on external sources like Osmose, not just on element selection.

## Changeset differences compared to StreetComplete
This section is aimed for people trying to decide whether a bad edit done in SCEE is fault of the user or of the app (SCEE modifications).
In general, SCEE changesets will contain changes very similar to StreetComplete changesets, with following differences:
* `created_by` is set to `StreetComplete_ee <version>`
* Quest type is given in `StreetComplete_ee:quest_type`
* _AddBuildingType_ has additional answers `barn`, `sty`, `stable`, `cowshed`, `digester`, `presbytery`, `riding_hall`, `sports_hall`, `tent`, `elevator`, and `transformer_tower`
* _AddCrossingType_ may change `crossing_ref`, `crossing:markings`, and `traffic_calming`
* _AddPathSurface_ and _AddRoadSurface_ have additional surfaces `metal_grid` and `stepping_stones`
* _AddMaxSpeed_ may tag `maxspeed:conditional`
* [discardable tags](https://wiki.openstreetmap.org/wiki/Discardable_tags) are removed automatically 
* Any node may be moved, even if it is part of a way or relation
* Any node may be deleted, or have all tags removed if it's not free-floating
* `check_date:*` may be added without resurvey
* Wheelchair quests may add `wheelchair:description` and `wheelchair:description:<language>`
* An element at at the same position as a note may be edited (this is blocked in normal SC)
* Most quests may apply to an extended range of elements (user-defined)
* Starting with SCEE 52.0, some answers create separate changesets with comment `Other edits in context of: <orignal quest changeset comment>`.
This happens for changes that can occur in StreetComplete, such as moving or deleting a node, changing shop types, removing surface, changing highway to steps and removing sidewalks.
Further SCEE adds new answers leading to such a changeset comment:
  * All quest types related to roads / paths may adjust access tags
  * Quests types asking about about benches, picnic tables, recycling containers, bicycle parkings and sports tracks/pitches may tag `access=private`
  * All quest types related to buildings may change `building` to `demolished:building`
* SCEE contains some additional quests, see [here (scroll to bottom)](app/src/main/java/de/westnordost/streetcomplete/quests/QuestsModule.kt)
  * These quests usually do not fulfill the requirements for StreetComplete, and need to be enabled by the user first.
  * There are the further quest types
  * _TagEditor_: may modify any tag
  * _CreatePoiEditType_: adds nodes, free floating or part of ways, (may change tags of existing way node instead of inserting a new one under some circumstances)
  * _CustomOverlay_: may modify tags, delete or move nodes

# original StreetComplete readme below

![StreetComplete](http://www.westnordost.de/streetcomplete/featureGraphic.png)

StreetComplete is an easy to use editor of OpenStreetMap data available for Android. It can be used without any OpenStreetMap-specific knowledge. It asks simple questions, with answers directly used to edit and improve OpenStreetMap data. The app is aimed at users who do not know anything about OSM tagging schemes but still want to contribute to OpenStreetMap.

StreetComplete automatically looks for nearby places where a survey is needed and shows them as quest markers on its map. Each of these quests can then be solved on site by answering a simple question. For example, tapping on a marker may show the question "What is the name of this road?", with a text field to answer it.
More examples are shown in the screenshots below.

The user's answer is automatically processed and uploaded directly into the OSM database. Edits are done in meaningful changesets using the user's OSM account.
Since the app is meant to be used on a survey, it can be used offline and is
economic with data usage.

To make the app easy to use, quests are limited to those answerable by asking simple questions.

* See the [latest release notes](https://github.com/streetcomplete/StreetComplete/releases).

## Screenshots
<img src="metadata/en-US/images/phoneScreenshots/screenshot1.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot2.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot3.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot4.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot5.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot6.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot7.png" width="240"/> <img src="metadata/en-US/images/phoneScreenshots/screenshot8.png" width="240"/>

## Download

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=de.westnordost.streetcomplete)[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/de.westnordost.streetcomplete/)[<img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Download APK from GitHub" height="80">](https://github.com/streetcomplete/StreetComplete/releases/latest)

## Quests

There are quite a few different quest types now and more will be added over time.
You can see a community-managed [list of all quests in the OSM wiki](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests).

## FAQ

You can find a list of [frequently asked questions in the wiki](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ).

## Contributing

This is an active open-source project, so you can get involved in it easily!
You can do so **without any programming or OpenStreetMap knowledge**! Just choose a task that you like.

Here are a few things you can do:
* 🐛 [Test and report issues](CONTRIBUTING.md#testing-and-reporting-issues)
* 📃 [Translate the app into your language](CONTRIBUTING.md#translating-the-app)
* 🕵️ [Solve notes left by StreetComplete users](CONTRIBUTING.md#solving-notes)
* 💡 [Suggest new quests](CONTRIBUTING.md#suggesting-new-quests), or, even better, [implement them](CONTRIBUTING.md#developing-new-quests).
* ➕ [and more…](CONTRIBUTING.md)

Also, if you like StreetComplete, **spread the word**! ❤️

## License

This software is released under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0.html).

## Sponsors

<a href="https://nlnet.nl/discovery/"><img src=".github/images/logo_nlnet.svg" height="100"/> <img src=".github/images/logo_ngi0.svg" height="100"/></a><br/>
<a href="https://nlnet.nl/discovery/">NGI Zero Discovery</a> is a grant program organized by the NLnet foundation which sponsored the development on this app in three individual grants:<br/>
Grants given to Mateusz Konieczny in <a href="https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849">2019</a> and <a href="https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/397825">2021</a> enabled him to work on StreetComplete for about one year in total.
Furthermore, yet another grant from <a href="https://nlnet.nl/project/StreetComplete-Together/">2021</a> enabled Tobias Zwick to work on the app for about 4-5 months!<br/>
<br/>

<a href="https://bmbf.de/"><img src=".github/images/logo_bmbf.png" height="160"/></a><a href="https://prototypefund.de/"><img src=".github/images/logo_prototypefund.svg" height="160"/></a><br/>
The <a href="https://bmbf.de/">German Federal Ministry of Education and Research</a> sponsored Tobias Zwick to work on this project (grant code 01IS20S35) within the frame of round 8 of the <a href="https://prototypefund.de/en/project/streetcomplete/">Prototype Fund</a> for about six months in 2020/2021.<br/>
<br/>

<a href="https://github.com/sponsors/westnordost"><img src=".github/images/logo_github.png" width="58"/></a> <a href="https://liberapay.com/westnordost"><img src=".github/images/logo_liberapay.svg" width="58"/></a> <a href="https://www.patreon.com/westnordost"><img src=".github/images/logo_patreon.png" width="58"/></a><br/>
Many people are currently supporting this app through <a href="https://github.com/sponsors/westnordost">GitHub sponsors</a>, <a href="https://liberapay.com/westnordost">Liberapay</a> and <a href="https://www.patreon.com/westnordost">Patreon</a>. If you like the app, you can join them ☺️ to support the continued support and maintenance of the app.<br/>
<br/>

<a href="https://www.jawg.io"><img src=".github/images/logo_jawgmaps.png" height="58"/></a><br>
<a href="https://www.jawg.io">JawgMaps</a> is a provider of online custom maps, geocoding and routing based on OpenStreetMap data. They are providing their vector map tiles service to StreetComplete for free, i.e. the background map displayed in the app.</td>
<br/>

<a href="https://osmfoundation.org/"><img src=".github/images/logo_osmf.png" height="58"/></a><br/>
The <a href="https://osmfoundation.org/">OpenStreetMap foundation</a> was funding the development of doing <a href="https://wiki.openstreetmap.org/wiki/Microgrants/Microgrants_2020/Proposal/Map_Maintenance_with_StreetComplete">map maintenance with StreetComplete</a> (~ 3 weeks) in their <a href="https://blog.openstreetmap.org/2020/07/01/osmf-microgrants-program-congratulations-to-selected-projects/">first round</a> of the <a href="https://wiki.osmfoundation.org/wiki/Microgrants">microgrant program</a> in 2020.
