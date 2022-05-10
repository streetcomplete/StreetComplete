package de.westnordost.streetcomplete.quests.surface

sealed class WrongSurfaceAnswer
class TracktypeIsWrong : WrongSurfaceAnswer()
class SpecificSurfaceIsWrong : WrongSurfaceAnswer()
