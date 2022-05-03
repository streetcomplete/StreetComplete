package de.westnordost.streetcomplete.data.osm.edits

interface ElementEditType : EditType {
    /** the name that is recorded as StreetComplete:quest_type=<name> in the changeset.
     *  Mostly for statistics purposes. Used to attribute in which context a change was made */
    //TODO val name: String get() = this::class.simpleName!!

    /** The changeset comment to be used when uploading to the OSM API. It should briefly explain
     * what is being changed (in English). */
    val changesetComment: String
}
