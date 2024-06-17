package de.westnordost.streetcomplete.screens.settings.quest_selection

    /** Contains the logic for drag and drop (for reordering)
    private inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        private var draggedFrom = -1
        private var draggedTo = -1

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val qv = (viewHolder as QuestSelectionViewHolder).item ?: return 0
            if (!qv.isInteractionEnabled) return 0

            return makeFlag(ACTION_STATE_IDLE, UP or DOWN) or
                   makeFlag(ACTION_STATE_DRAG, UP or DOWN)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            Collections.swap(quests, from, to)
            notifyItemMoved(from, to)
            return true
        }

        override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val qv = (target as QuestSelectionViewHolder).item ?: return false
            return qv.isInteractionEnabled
        }

        override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            if (draggedFrom == -1) draggedFrom = fromPos
            draggedTo = toPos
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ACTION_STATE_IDLE) {
                onDropped()
            }
        }

        private fun onDropped() {
            /* since we modify the quest list during move (in onMove) for the animation, the quest
             * type we dragged is now already at the position we want it to be. */
            if (draggedTo != draggedFrom && draggedTo > 0) {
                val item = quests[draggedTo].questType
                val toAfter = quests[draggedTo - 1].questType

                viewModel.orderQuest(item, toAfter)
            }
            draggedFrom = -1
            draggedTo = -1
        }

        override fun isItemViewSwipeEnabled() = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
     */


/*
            binding.visibilityCheckBox.setOnClickListener {
                if (!item.selected && item.questType.defaultDisabledMessage != 0) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.enable_quest_confirmation_title)
                        .setMessage(item.questType.defaultDisabledMessage)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            viewModel.selectQuest(item.questType, true)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> binding.visibilityCheckBox.isChecked = false }
                        .setOnCancelListener { binding.visibilityCheckBox.isChecked = false }
                        .show()
                } else {
                    viewModel.selectQuest(item.questType, !item.selected)
                }
            }

            binding.dragHandle.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> itemTouchHelper.startDrag(this)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }
    */
