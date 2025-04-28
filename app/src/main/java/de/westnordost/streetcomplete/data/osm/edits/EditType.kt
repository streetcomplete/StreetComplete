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
    val achievements: List<EditTypeAchievement> get() = emptyList()

    /** the string resource id that explains why this edit type is disabled by default or zero if it
     *  is not disabled by default.
     *
     *  E.g. quest types that do not fully fulfill the [quest guidelines](https://github.com/streetcomplete/StreetComplete/blob/master/QUEST_GUIDELINES.md),
     *  (e.g. often the requirement that the information is publicly available from the outside),
     *  are disabled by default. */
    val defaultDisabledMessage: Int get() = 0
}
