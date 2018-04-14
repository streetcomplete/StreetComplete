Thanks for your interest in contributing to StreetComplete! 👍 There are many easy tasks you can do, **even without programming knowledge**, to create a better app for all users of StreetComplete and drive the OpenStreetMap project forward.

Even if you do not find something to do in this list, using StreetComplete, testing it and giving constructive feedback is always a valuable contribution too.

Content:
* [Translating the app](#translating-the-app)
* [Helping mappers by solving their notes](#helping-mappers-by-solving-their-notes)
* [Testing and reporting issues](#testing-and-reporting-issues)
   * [Issues of dependencies](#issues-of-dependencies)
   * [Suggesting new quests](#suggesting-new-quests)
* [Improving documentation](#improving-documentation)
* [Development](#development)
   * [Developing new quests](#developing-new-quests)

## Translating the app

You can translate StreetComplete at POEditor. You can add a translation in your own language or improve other translations. Note that manual translations as Pull Requests will not be merged and do not help the project.

Follow [**this link** to improve the translations](https://poeditor.com/join/project/IE4GC127Ki):

[![POEditor](https://poeditor.com/public/images/logo_small.png)](https://poeditor.com/join/project/IE4GC127Ki)

Before each big release, translations are pulled in from POEditor.

## Helping mappers by solving their notes

As you probably noticed, you can choose "Cannot answer" in StreetComplete and thus leave a note on OpenStreetMap.

You can help with [processing OSM notes opened by StreetComplete users](https://ent8r.github.io/NotesReview/?query=StreetComplete&limit=100&start=true). (You can also do it without that special tool, but it helps.) When doing so, you can also find systematic problems or misunderstandings of user's when solving StreetComplete quests.

If you find such user experience problems, please report them back in the [issue tracker of StreetComplete](https://github.com/westnordost/StreetComplete/issues). Do not forget to add links to examples, e.g. the notes StreetComplete mappers submitted.

## Testing and reporting issues

If you experience problems, crashes or a quest is not clear to you, feel free to open an issue for that. Remember to open one issue _for one matter_, so do not open one issue "I have found several problems", but one for each problem.

1. If you have questions, remember to [read the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) first.
2. Remember to report map style issues [in the appropriate repository](#issues-of-dependencies).
3. Look [whether your issue has already been reported](https://github.com/westnordost/StreetComplete/issues).
4. Now you can open a new issue. Give it a meaningful title and describe the problem. Do mention the StreetComplete and Android version, if it is a technical bug.

### Issues of dependencies

StreetComplete depends on some projects for the app. Try to find the appropriate place for reporting them.

* **Map style issues** should be reported in a [separate repository](https://github.com/ENT8R/streetcomplete-mapstyle).
Examples of such issues are missing elements on the map, display errors on the map, etc. As a rule of thumb, you can report everything that happens "behind" the quest icon markers there.

Note that this app has other dependencies. For reporting issues, in these, you however have to have some technical knowledge. So if you are **not sure** that the component listed below is responsible for the issue you have, it is often better to report them in the general StreetComplete issue tracker. People will then let you know whether this issue is solvable by StreetComplete or is an issue of Tangram-ES.

More dependencies:
* [Tangram-ES](https://github.com/tangrams/tangram-es/) for technical issues with the map rendering
* [countryboundaries](https://github.com/westnordost/countryboundaries) for detecting, in which country a quest is (affects quest display, etc.)
* [osmapi](https://github.com/westnordost/osmapi) for communication with the OSM API

### Suggesting new quests

We appreciate all quest ideas you and others have, but some ideas cannot be added into the app due to various design decisions and requirements. This is required in order to make a great app for all users of StreetComplete, whose contributions actually help the OpenStreetMap project. A quest which nobody can solve and where users potentially choose wrong options adding wrong data to OpenStreetMap hurts not only OSM, but also this app.

What you need to do:

1. Search whether the quest you want to suggest is [already implemented in StreetComplete](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests) or [already suggested in the issue tracker](https://github.com/westnordost/StreetComplete/issues).
2. Please read the full guide on what requirements you need to consider to make the quest a great thing: [Adding new Quests to StreetComplete](https://github.com/westnordost/StreetComplete/wiki/Adding-new-Quests-to-StreetComplete).
3. Open a new issue and fill out the issue template. Explain why your quest meets the criteria you need to check, so other users can follow the example.
4. Wait for input of others, even if you could implement it right away.  It is always suggested to _first_ suggest a quest and implement it only if it has been accepted there.

➡️ If you can code, see also how to [develop your own quest](#developing-new-quests).

A quest is officially marked as accepted when it get's [the "new quest" tag](https://github.com/westnordost/StreetComplete/labels/new%20quest).
<!-- TODO: introduce a better way/new tag here? ref https://github.com/westnordost/StreetComplete/issues/1006 -->

## Improving documentation

You can also help to keep the OpenStreetMap wiki **up-to-date** for StreetComplete. For example:
* Add missing quests in [the quest list](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests) or check, that they are up to date.
* Edit [the FAQ](https://wiki.openstreetmap.org/wiki/StreetComplete/FAQ) and add reoccuring questions.
* Edit [the main StreetComplete page](https://wiki.openstreetmap.org/wiki/StreetComplete).
* Edit [the JSON of all tags used by StreetComplete](https://github.com/goldfndr/StreetCompleteJSON) that is displayed on [taginfo](https://taginfo.openstreetmap.org/projects/streetcomplete).

## Development

If you would like to help and are able to contribute code, you are most welcome.

There are many reasonable feature requests and ideas for new question types in the issue tracker which you could also engage yourself with. If you have own ideas how to improve this app and want to make sure that the Pull Request will be merged, it is strongly suggested to **open an issue first** to discuss the feature.

Note StreetComplete also uses [some dependencies](#issues-of-dependencies), where contributions are likely also accepted and help StreetComplete, too.

If you need to find things where help is appreciated [have a look at the issues tagged with "help wanted"](https://github.com/westnordost/StreetComplete/labels/help%20wanted).

To start developing you can just download [Android Studio](https://developer.android.com/studio/index.html) and create your quest. It should automatically handle indentation through the [editorconfig file](.editorconfig). If you use another IDE, you may need to [install a plugin](http://editorconfig.org/#download).

<!-- TODO @westnordost: Add coding guidelines -->

### Developing new quests

You want to contribute a new quest right away? That's great!
However, we strongly suggest to [**open an issue** discussing the quest](#suggesting-new-quests), before creating a PR, so we can see if your quest idea meets the criteria and can be included in StreetComplete.

In case you **don't have an idea of a quest?** Look [at the existing issues](https://github.com/westnordost/StreetComplete/issues?q=is%3Aissue+is%3Aopen+label%3A%22new+quest%22+sort%3Areactions-%2B1-desc). Sorted by 👍 reactions you can also see which are the most requested quests.

Always remember to pay attention to [the new quest guide](https://github.com/westnordost/StreetComplete/wiki/Adding-new-Quests-to-StreetComplete) of StreetComplete! It also has tips for implementing a quest.

You can look at **other quests** to see how they are implemented.
