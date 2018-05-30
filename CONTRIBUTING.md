Thanks for your interest in contributing to StreetComplete! 👍 There are many easy tasks you can do, **even without programming knowledge**, to create a better app for all users of StreetComplete and drive the OpenStreetMap project forward.

Even if you do not find something to do in this list, using StreetComplete, testing it and giving constructive feedback is always a valuable contribution too.

Content:
* [Translating the app](#translating-the-app)
* [Solving notes](#solving-notes)
* [Testing and reporting issues](#testing-and-reporting-issues)
   * [Issues of dependencies](#issues-of-dependencies)
   * [Suggesting new quests](#suggesting-new-quests)
* [Improving documentation](#improving-documentation)
* [Development](#development)
   * [Developing new quests](#developing-new-quests)
   * [StreetComplete-related projects](#streetcomplete-related-projects).

## Translating the app

You can translate StreetComplete at POEditor. You can add a translation in your own language or improve other translations. Note that manual translations as Pull Requests will not be merged and do not help the project.

Follow [**this link** to improve the translations](https://poeditor.com/join/project/IE4GC127Ki):

[![POEditor](https://poeditor.com/public/images/logo_small.png)](https://poeditor.com/join/project/IE4GC127Ki)

Before each release, translations are pulled in from POEditor.

## Solving notes

As you probably noticed, you can choose *"Cannot answer"* in StreetComplete and thus leave a note on OpenStreetMap.

You can help with [processing OSM notes opened by StreetComplete users](https://ent8r.github.io/NotesReview/?query=StreetComplete&limit=100&start=true). While processing and solving notes, it may become apparent that there is a systematic problem in that users misunderstand the UI or the wording when solving StreetComplete quests.

If you find such user experience problems, please report them back in the [issue tracker of StreetComplete](https://github.com/westnordost/StreetComplete/issues). Do not forget to add links to examples, e.g. the notes StreetComplete mappers submitted.

## Testing and reporting issues

If you experience problems, crashes or a quest is not clear to you, feel free to open an issue for that. Remember to open one issue _for one matter_, so do not open one issue "I have found several problems", but one for each problem. Before you open an issue, please consider: 

1. If you have questions, remember to [read the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) first.
2. Remember to report map style issues [in the appropriate repository](#issues-of-dependencies).
3. Look [whether your issue has already been reported](https://github.com/westnordost/StreetComplete/issues).

### Issues of dependencies

StreetComplete depends on some projects for the app. Try to find the appropriate place for reporting them.

* **Map style issues** should be reported in a [separate repository](https://github.com/ENT8R/streetcomplete-mapstyle).
Examples of such issues are missing elements on the map, display errors on the map, etc. As a rule of thumb, you can report everything that happens "behind" the quest icon markers there.

Note that this app has other dependencies. For reporting issues, in these, you however have to have some technical knowledge. So if you are **not sure** that the component listed below is responsible for the issue you have, it is often better to report them in the general StreetComplete issue tracker. People will then let you know whether this issue is solvable by StreetComplete or is an issue of Tangram-ES.
The full list of dependencies and other StreetComplete-related projects is listed [at the end of this file](#streetcomplete-related-projects).

### Suggesting new quests

Not all ideas for quests are actually eligible to be included in this app. So, before you suggest a new quest, it is very important that you read the [Quest Guidelines for StreetComplete](https://github.com/westnordost/StreetComplete/wiki/Adding-new-Quests-to-StreetComplete).

If you can code, see also how to [develop your own quest](#developing-new-quests).

## Improving documentation

You can also help to keep the OpenStreetMap wiki **up-to-date** for StreetComplete. For example:
* Add missing quests in [the quest list](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests) or check, that they are up to date.
* Edit [the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) and add reoccurring questions.
* Edit [the main StreetComplete page](https://wiki.openstreetmap.org/wiki/StreetComplete).
* Edit [the JSON of all tags used by StreetComplete](https://github.com/goldfndr/StreetCompleteJSON) that is displayed on [taginfo](https://taginfo.openstreetmap.org/projects/streetcomplete).

## Development

If you would like to help and are able to contribute code, you are most welcome.

There are many reasonable feature requests and ideas for new question types in the issue tracker which you could also engage yourself with. If you have own ideas how to improve this app and want to make sure that the Pull Request will be merged, it is strongly suggested to **open an issue first** to discuss the feature, especially if you aim to add a new quest, [see below](#developing-new-quests).

Note that StreetComplete also uses [some dependencies](#issues-of-dependencies), where contributions are likely also accepted and help StreetComplete, too.

If you need to find things where help is appreciated [have a look at the issues tagged with "help wanted"](https://github.com/westnordost/StreetComplete/labels/help%20wanted).

### Developing new quests

You want to contribute a new quest right away? That's great!
However, we strongly suggest to [**open an issue** discussing the quest](#suggesting-new-quests), before creating a PR, so we can see if your quest idea meets the criteria and can be included in StreetComplete.

In case you **don't have an idea of a quest?** Look [at the existing issues](https://github.com/westnordost/StreetComplete/issues?q=is%3Aissue+is%3Aopen+label%3A%22new+quest%22+sort%3Areactions-%2B1-desc). Sorted by 👍 reactions you can also see which are the most requested quests.

Always remember to pay attention to [the quest guidelines](https://github.com/westnordost/StreetComplete/wiki/Adding-new-Quests-to-StreetComplete) of StreetComplete! It also has tips for implementing a quest.

### StreetComplete-related projects

#### Dependencies

* [Tangram-ES](https://github.com/tangrams/tangram-es/) for technical issues with the map rendering
* [countryboundaries](https://github.com/westnordost/countryboundaries) for detecting, in which country a quest is (affects quest display, etc.)
* [osmapi](https://github.com/westnordost/osmapi) for communication with the OSM API

#### Created for StreetComplete

* [StreetCompleteJSON](https://github.com/goldfndr/StreetCompleteJSON) by [@goldfndr](https://github.com/goldfndr) listing all tags edited by StreetComplete for [taginfo](https://taginfo.openstreetmap.org/projects/streetcomplete)
* [blacklistr](https://github.com/ENT8R/blacklistr) by [@ENT8R](https://github.com/ENT8R) for visualizing StreetComplete's country exclusion list
* [NotesReview](https://github.com/ENT8R/NotesReview) by [@ENT8R](https://github.com/ENT8R) for reviewing notes with a specific keyword (here: `StreetComplete`)
* [streetcomplete-mapstyle](https://github.com/ENT8R/streetcomplete-mapstyle) by [@ENT8R](https://github.com/ENT8R) maintaining the mapstyle of StreetComplete
* [oneway-data-api](https://github.com/ENT8R/oneway-data-api) by [@ENT8R](https://github.com/ENT8R) for getting oneway data from improveosm.org
* [crops-parser](https://github.com/rugk/crops-parser) by [@rugk](https://github.com/rugk) for parsing data for the orchard quest
* [streetcomplete-quest-svgs](https://github.com/rugk/streetcomplete-quest-svgs) by [@rugk](https://github.com/rugk) for having minified versions of all StreetComplete quest icons for the OSM wiki
* [streetcomplete-ad-34c3](https://github.com/rugk/streetcomplete-ad-34c3) by [@rugk](https://github.com/rugk) as a banner advertisement

You may find more projects under [the StreetComplete tag](https://github.com/topics/streetcomplete) on GitHub.

