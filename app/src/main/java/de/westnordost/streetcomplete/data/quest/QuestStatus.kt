package de.westnordost.streetcomplete.data.quest

enum class QuestStatus {
    /** just created. AKA "open" */
    NEW,
    /** user answered the question (waiting for changes to be uploaded)  */
    ANSWERED,
    /** user chose to hide the quest. He may un-hide it later (->NEW). */
    HIDDEN,
    /** the system (decided that it) doesn't show the quest. They may become visible again (-> NEW)  */
    INVISIBLE,
    /** the quest has been uploaded (either solved or dropped through conflict). The app needs to
     * remember its solved quests for some time before deleting them so that they can be reverted
     * Note quests are generally closed after upload, they are never deleted  */
    CLOSED,
    /** the quest has been closed and after that the user chose to revert (aka undo) it. This state
     * is basically the same as CLOSED, only that it will not turn up in the list of (revertable)
     * changes. Note, that the revert-change is done via another Quest upload, this state is only
     * to mark this quest as that a revert-quest has already been created */
    REVERT; // TODO remove completely?

    val isVisible: Boolean get() = this == NEW
}
