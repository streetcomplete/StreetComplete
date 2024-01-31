Thanks for your interest in contributing to StreetComplete! 👍 There are many easy tasks you can do, **even without programming knowledge**, to create a better app for all users of StreetComplete and drive the OpenStreetMap project forward.

Even if you do not find something to do in this list, using StreetComplete, testing it and giving constructive feedback is always a valuable contribution too.

Content:

   * [Translating the app](#translating-the-app)
   * [Solving notes](#solving-notes)
   * [Testing and reporting issues](#testing-and-reporting-issues)
   * [Issues with dependencies](#issues-with-dependencies)
   * [Suggesting new quests](#suggesting-new-quests)
   * [Improving documentation](#improving-documentation)
   * [Development](#development)
   * [Developing new quests](#developing-new-quests)
   * [StreetComplete-related projects](#streetcomplete-related-projects).

## Translating the app

You can translate StreetComplete at POEditor. You can add missing translations and improve existing ones. Discuss translations at POEditor.

The only required skills here are ability to read English text and write in your own language.

Follow [**this link** to improve the translations](https://poeditor.com/join/project/IE4GC127Ki):

[![POEditor](https://poeditor.com/public/images/logo_small.png)](https://poeditor.com/join/project/IE4GC127Ki)

After joining, the [main site of the POEditor](https://poeditor.com/projects/) should list StreetComplete for logged in users.

Before each [release](res/documentation/creating%20new%20release.md), translations are pulled in from POEditor. Please, use POEditor for translating. Manual changes submitted as Pull Requests will not be merged as they do not help the project.

Once 100% or close to 100% of text is translated, the given language becomes enabled. Translations which are not maintained are removed. Typically languages which are less than 60% translated will be considered as not maintained and such translation will be disabled.

The source translation is in US English (in [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml)). POEditor has a list of modifications in dialects such as UK English and Australian English, listed as separate languages.

### iD presets

Some translations are from iD presets. For example in "Is this still here (Bench)" text "Is this still here ()" will be translated as part of StreetComplete, but translation for bench is taken from iD presets.

For iD preset translation see [their documentation](https://github.com/openstreetmap/iD/blob/develop/CONTRIBUTING.md#translating).

## Solving notes

As you probably noticed, you can choose *"Cannot answer"* in StreetComplete and thus leave a note on OpenStreetMap.

You can help with [processing OSM notes opened by StreetComplete users](https://ent8r.github.io/NotesReview/?query=StreetComplete&limit=100&start=true). While processing and solving notes, it may become apparent that there is a systematic problem in that users misunderstand the UI or the wording when solving StreetComplete quests.

If you find such user experience problems, please report them back in the [issue tracker of StreetComplete](https://github.com/streetcomplete/StreetComplete/issues). Do not forget to add links to examples, e.g. the notes StreetComplete mappers submitted.

## Testing and reporting issues

If you experience problems, crashes or a quest is not clear to you, feel free to open an issue for that. Remember to open one issue _for one matter_, so do not open one issue "I have found several problems", but one for each problem. Before you open an issue, please consider: 

1. If you have questions, remember to [read the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) first.
2. Remember to report map style issues [in the appropriate repository](#issues-with-dependencies).
3. Look [whether your issue has already been reported](https://github.com/streetcomplete/StreetComplete/issues) (remember to check closed issues too, it may have already been fixed and is due to appear in the next release).

### Issues with dependencies

StreetComplete depends on some projects for the app. Try to report them in the appropriate place.

* **Map style issues** should be reported in a [separate repository](https://github.com/ENT8R/streetcomplete-mapstyle).
Examples of such issues are missing elements on the map, display errors on the map, etc. As a rule of thumb, you can report everything that happens "behind" the quest icon markers there.

Note that this app has other dependencies. For reporting issues in these, you have to have some technical knowledge. So if you are **not sure** that the component listed below is responsible for the issue you have, it is often better to report them in the general StreetComplete issue tracker. People will then let you know whether this issue is solvable by StreetComplete or if it is an issue with Tangram-ES or another project.
The full list of dependencies and other StreetComplete-related projects is listed [at the end of this file](#streetcomplete-related-projects).

### Suggesting new quests

Not all ideas for quests are actually eligible to be included in this app. So, before you suggest a new quest, it is very important that you read the [Quest Guidelines for StreetComplete](https://github.com/streetcomplete/StreetComplete/blob/master/QUEST_GUIDELINES.md).

If you can code, see also how to [develop your own quest](#developing-new-quests).

## Improving documentation

You can also help to keep the OpenStreetMap wiki **up-to-date** for StreetComplete. For example:
* Add missing quests in [the quest list](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests) or check, that they are up to date; there is a [helper action](https://github.com/streetcomplete/StreetComplete/actions/workflows/generate-quest-list.yml) which generates a CSV for this.
* Edit [the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) and add reoccurring questions.
* Edit [the main StreetComplete page](https://wiki.openstreetmap.org/wiki/StreetComplete).

## Development

If you would like to help and are able to contribute code, you are most welcome.

There are many feature requests and ideas for new question types in the issue tracker which you can use. If you have own ideas on how to improve this app and want to make sure that the pull request will be merged, it is strongly suggested you **open an issue first** to discuss the feature, especially if you aim to add a new quest, [see below](#developing-new-quests).

Note that StreetComplete also uses [some dependencies](#issues-with-dependencies), where contributions are likely also accepted and help StreetComplete, too.

If you need to find things where help is especially appreciated [have a look at the issues tagged with "help wanted"](https://github.com/streetcomplete/StreetComplete/labels/help%20wanted).

To build and test StreetComplete [download and install Android Studio](https://developer.android.com/studio/) which comes bundled with all tools needed, checkout and open the project in this application and click on the green play button on the top. If your first build fails due to missing dependencies, make sure [you have accepted the required license agreements](https://developer.android.com/studio/intro/update#download-with-gradle).

### Developing new quests

You want to contribute a new quest right away? That's great!
However, we strongly suggest you [**open an issue** discussing the quest](#suggesting-new-quests), before creating a PR, so we can see if your quest idea meets the criteria and can be included in StreetComplete.

In case you **don't have an idea of a quest?** Look [at the existing issues](https://github.com/streetcomplete/StreetComplete/issues?q=is%3Aissue+is%3Aopen+label%3A%22new+quest%22+sort%3Areactions-%2B1-desc). Sorted by 👍 reactions you can also see which are the most requested quests.

Always remember to pay attention to [the quest guidelines](https://github.com/streetcomplete/StreetComplete/blob/master/QUEST_GUIDELINES.md) of StreetComplete! It also has tips for implementing a quest.

See also [this far more detailed guide to making a new quest](CONTRIBUTING_A_NEW_QUEST.md).

### Code style

Inheritance and class hierarchy should be avoided if possible. It is preferable to extract shared code to helper file such as [KerbUtil.kt](app/src/main/java/de/westnordost/streetcomplete/osm/kerb/KerbUtil.kt).

It is recommended to install the [*Ktlint* Android Studio plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) which highlights lint issues (e.g. inconsistent spacing) directly inline while writing code.

### Hints for more active people

See ["Efficiently working together"](https://github.com/streetcomplete/StreetComplete/discussions/3450) which is addressed to people highly active in this project.

If you are making your first issue or pull request then you can definitely skip reading it.

Materials in [`res/documentation`](res/documentation) also may be useful, it includes

* [Checklist for creating a release](res/documentation/creating%20new%20release.md)
* [Presentation discussing data model allowing offline editing, undo, splitting ways and resolving edit conflicts](res/documentation/how-it-handles%20edits.odp)

## StreetComplete-related projects

### Dependencies

* [Tangram-ES](https://github.com/tangrams/tangram-es/) map rendering
* [countryboundaries](https://github.com/westnordost/countryboundaries) for detecting in which country a quest is (affects quest display, etc.)
* [countrymetadata](https://github.com/streetcomplete/countrymetadata) for info about various countries (left/right hand driving etc.)
* [osmapi](https://github.com/westnordost/osmapi) for communication with the OSM API
* [osmfeatures](https://github.com/westnordost/osmfeatures) to correctly refer to a feature by name. Data for it is provided by:
  * [iD Tagging Schema](https://github.com/openstreetmap/id-tagging-schema) - In StreetComplete it powers display of feature type, object classification in shop overlay and more
  * [Name suggestion index](https://github.com/osmlab/name-suggestion-index) - it allows to search POI types also by brand names
* [streetcomplete-mapstyle](https://github.com/streetcomplete/streetcomplete-mapstyle) by [@ENT8R](https://github.com/ENT8R) maintaining the mapstyle of StreetComplete

### Created for StreetComplete

* [parser](https://github.com/matkoniecz/Zazolc/tree/taginfo) by [@matkoniecz](https://github.com/matkoniecz) listing tags added by StreetComplete for [taginfo](https://taginfo.openstreetmap.org/projects/streetcomplete#tags)
* [blacklistr](https://github.com/ENT8R/blacklistr) by [@ENT8R](https://github.com/ENT8R) for visualizing StreetComplete's country exclusion list
* [NotesReview](https://github.com/ENT8R/NotesReview) by [@ENT8R](https://github.com/ENT8R) for reviewing notes with a specific keyword (here: `StreetComplete`)
* [oneway-data-api](https://github.com/streetcomplete/oneway-data-api) by [@ENT8R](https://github.com/ENT8R) for getting oneway data from improveosm.org
* [crops-parser](https://github.com/rugk/crops-parser) by [@rugk](https://github.com/rugk) for parsing data for the orchard quest
* [streetcomplete-ad-c3](https://github.com/rugk/streetcomplete-ad-c3) by [@rugk](https://github.com/rugk) as a banner advertisement
* [sc-photo-service](https://github.com/streetcomplete/sc-photo-service) by [@exploide](https://github.com/exploide) allows StreetComplete to upload photos associated with OSM Notes
* [sc-statistics-service](https://github.com/streetcomplete/sc-statistics-service) by [@westnordost](https://github.com/westnordost) aggregates and provides StreetComplete-related statistics about users.
* [StreetComplete-taginfo-categorize](https://github.com/mnalis/StreetComplete-taginfo-categorize) by [@mnalis](https://github.com/mnalis) generates tags listed in [KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED](https://github.com/streetcomplete/StreetComplete/blob/master/app/src/main/java/de/westnordost/streetcomplete/osm/Shop.kt#L6)

You may find more projects under [the StreetComplete tag](https://github.com/topics/streetcomplete) on GitHub.
