# Quest data flow

StreetComplete queries external services to find quest candidates.

Every quest type has defined some properties, including [Overpass](https://wiki.openstreetmap.org/wiki/Overpass_API) queries. The Overpass instance is queried and responds with OpenStreetMap data matching rules for quest candidates.

Query responses are used to build a local quest database, displayed to the user as markers on the map. Once a user solves a quest, the solution is stored in the local database as a diff. Changes are [uploaded to OSM](https://wiki.openstreetmap.org/wiki/API_v0.6) as soon as possible. Changes are made in the OSM database using credentials provided by the user. Edits are grouped into changesets by quest types.

The definition of quests in the program may be simple, with just few parameters and made with reusable blocks, like [quest asking whatever toilet is paid](https://github.com/westnordost/StreetComplete/blob/master/app/src/main/java/de/westnordost/streetcomplete/quests/toilets_fee/AddToiletsFee.kt). Some definitions are highly complicated, defining special interfaces, using [country specific data](https://github.com/westnordost/StreetComplete/tree/master/res/country_metadata) or involve special processing of data. [The quest asking about house number](https://github.com/westnordost/StreetComplete/tree/master/app/src/main/java/de/westnordost/streetcomplete/quests/housenumber) is a good example of a quest handling quite complex situations. For starters, not the entire world has the same numbering system – some countries have block-based addressing or addresses with [more than one](https://wiki.openstreetmap.org/wiki/Key:addr:conscriptionnumber) assigned house number.

Some quests may be based on other data sources. The [note quest](https://github.com/westnordost/StreetComplete/tree/master/app/src/main/java/de/westnordost/streetcomplete/quests/note_discussion) is based on data directly downloaded from the [OSM API](https://wiki.openstreetmap.org/wiki/API_v0.6#Map_Notes_API). The [oneway quest](https://github.com/westnordost/StreetComplete/tree/master/app/src/main/java/de/westnordost/streetcomplete/quests/oneway) is using [an external list of roads likely to be a oneway](https://github.com/ENT8R/oneway-data-api).

The note quest is also special because part of the answer – photos made by users – is uploaded to a [special photo service](https://github.com/exploide/sc-photo-service), as OSM notes do not allow hosting of images directly on OSM servers.

# Map

SC downloads the [vector tiles](https://github.com/tilezen/vector-datasource) used for displaying its map from an external source and renders them using the library [tangram-es](https://github.com/tangrams/tangram-es).
