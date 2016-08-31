package de.westnordost.osmagent;

import dagger.Component;
import de.westnordost.osmagent.quests.QuestController;
import de.westnordost.osmagent.quests.DbModule;

@Component(modules = DbModule.class)
public interface OsmagentDI
{
	QuestController makeQuestController();
}
