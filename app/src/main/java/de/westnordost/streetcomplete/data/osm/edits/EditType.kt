package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

interface EditType {
    /** the icon resource id used to display this edit type (in edit history, statistics, on map if
     *  applicable, ...) */
    val icon: Int

    /** the string resource id used to display this edit type (in edit history, statistics, ...) */
    val title: Int

    /** the name that is recorded as StreetComplete:quest_type=<name> in the changeset and to
     *  identify it in the database, statistics etc. , i.e. used to attribute in which context a
     *  change was made */
    val name: String get() = this::class.simpleName!!

    /** The OpenStreetMap wiki page with the documentation for the tag or feature that is being
     *  edited by this quest type */
    val wikiLink: String?

    /** towards which achievements solving an edit of this type should count */
    val achievements: List<EditTypeAchievement>
}
