#osmagent#

osmagent is an Android app in the making that should enable ingenuous users to contribute to the OpenStreetMap without having to use another editor or know anything about the data structures or tagging schemes in OSM.
The program searches for wrong, incomplete or extendable data in the user's vicinity and presents the user with a map of quests (like i.e. in Mapdust) that each are solvable by filling out a simple form to complete/correct the information while surveying it.

## State of development

From the point of view of targeting a first functional release, not from what I plan for it later...

### What's more or less done
* Authentication via OAuth
* Communication with the OSM Api - using [osmapi](https://github.com/westnordost/osmapi) (i.e. Uploading changes)
* Framework for collecting incomplete, wrong or extendable data

### What's missing
* "Recipies" (aka "Quests") for incomplete data and...
* Forms / dialogs to complete this data
* A proper map view
* Architecture that takes care of the whole workflow: download/create quests, put them in a local DB, display them on the map, show the dialog, get result from the dialog and change the data, upload the data, delete from local DB

## License

This software is released under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0.html).