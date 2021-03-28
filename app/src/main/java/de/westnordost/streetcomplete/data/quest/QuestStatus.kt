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
    CLOSED;

    val isVisible: Boolean get() = this == NEW
}
