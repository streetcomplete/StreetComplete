package de.westnordost.streetcomplete.data.osm.edits

interface ElementEditType : EditType {
    /** The changeset comment to be used when uploading to the OSM API. It should briefly explain
     * what is being changed (in English). */
    val changesetComment: String
}
