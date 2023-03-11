SCEE is a modified version of StreetComplete, aimed at experienced OSM users unhappy about the lack of advanced editing capabilities in normal StreetComplete.

Please be aware that SCEE is not suitable for people used to discard warning messages without reading!
Users new to OpenStreetMap are best advised to use StreetComplete.

Functionality added in SCEE is considerably less tested than what you might be used from StreetComplete, so bugs or unexpected behavior may happen. If you encounter any, please report the issue.

Due to the different name used in changesets, edits made with this version do not contribute to the displayed StreetComplete statistics and star count.

## Notable changes
* Option to directly edit tags
* More customizable behavior, including settings or element selection for many quests
  * Such customization should be handled with care. There are some safeguards, but modifying element selection could still lead to app crashes or bad tagging.
* Overlay with customizable element selection and color source (regular expression evaluating tags)
* Create nodes anywhere, using presets from iD tagging schema for pre-filling tags
* Ability to create notes in a GPX file instead of uploading them to OSM
* Additional quests that are not eligible for StreetComplete, usually because some answers cannot be tagged, or because not everyone has the required knowledge to answer the quest
* Quests based on external data sources (currently available: Osmose and CSV file)
* Switch to satellite / aerial imagery background, and customize the source URL
* Quick settings, for fast switch of preset or background
* Level filter (available only in quick settings menu)
* Import / export for settings, quest presets, hidden quests and custom overlays
* Show all quests for the selected element at once, and also nearby quests
* Show direction of ways, which may be useful for tagging oneways, or for solving Osmose issues
* More answers for some quests, like additional building types or specifying that a crossing is raised
* Show all hidden quests on long press on undo icon (works only for downloaded areas)
* Switch the main menu to a grid with six full-size buttons
* Downloading data will interrupt running uploads (will resume afterwards)
* Allow creating quests on the fly, which immediately reflects changes in resurvey interval and element selection of a quest
* Display a track from a GPX file, e.g. for following a planned route while surveying
* Different app name, which means edits made with this version will not be counted in StreetComplete statistics (used in notes and changesets: StreetComplete_ee)

A more detailed list of changes can be found in the [changelog](app/src/main/res/raw/changelog_ee.yml).
Database and preferences files are compatible with StreetComplete, so if you have root privileges you can transfer them in either direction.

## Permissions
SCEE has more permissions than StreetComplete: `ACCESS_BACKGROUND_LOCATION` and `POST_NOTIFICATIONS`. Both are used only in an experimental feature to notify about nearby quests when the app is in background.

## Download SCEE

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/de.westnordost.streetcomplete.expert/)
[<img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Download APK from GitHub" height="80">](https://github.com/Helium314/SCEE/releases/latest)

## Translations
Translations for strings added in SCEE can be done [using Weblate](https://translate.codeberg.org/projects/scee/).

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
* _AddBuildingType_ has additional answers `barn`, `sty`, `stable`, `cowshed`, `digester`, `presbytery`, `riding_hall`, `sports_hall`, and `transformer_tower`
* _AddCrossingType_ may change `crossing_ref`, `crossing:markings`, and `traffic_calming`
* _AddPathSurface_ and _AddRoadSurface_ have additional surfaces `metal_grid` and `stepping_stones`
* Any node may be moved, even if it is part of a way or relation
* Any node may be deleted, or have all tags removed if it's not free-floating
* `check_date:*` may be added without resurvey
* Wheelchair quests may add `wheelchair:description` and `wheelchair:description:<language>`
* An element at at the same position as a note may be edited (this is blocked in normal SC)
* Most quests may apply to an extended range of elements (user-defined)
* Starting with SCEE 52.0, some answers create separate changesets with comment `Other edits in context of: <orignal quest changeset comment>`.
This happens for changes that can occur in StreetComplete, such as moving or deleting a node, changing shop types, removing surface, changing highway to steps and removing sidewalks.
Further SCEE adds new answers leading to such a changeset comment:
  * All quest types related to roads / paths may add `access=private`
  * All quest types related to buildings may change `building` to `demolished:building`
* SCEE contains some additional quests, see [here (scroll to bottom)](app/src/main/java/de/westnordost/streetcomplete/quests/QuestsModule.kt)
  * These quests usually do not fulfill the requirements for StreetComplete, and need to be enabled by the user first.
  * There are the further quest types _TagEditor_ (may modify any tag), _CreatePoiEditType_ (adds nodes) and _CustomOverlay_ (may delete or move nodes; modifying tags in custom overlay context is done via _TagEditor_)

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
Furthermore, yet another grant from <a href="https://nlnet.nl/project/StreetComplete-Together/">2021</a> is enabling Tobias Zwick to work on the app for about 4-5 months!<br/>
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
