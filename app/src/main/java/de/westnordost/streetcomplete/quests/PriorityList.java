package de.westnordost.streetcomplete.quests;

import java.util.List;

import de.westnordost.streetcomplete.view.Item;

public class PriorityList {
    public static List<Item> buildList(List<Item> allItems, List<String> popularNames){
        // in reverse because the first element in the list should be first in religionsList
        for (int i = popularNames.size()-1; i >= 0; --i)
        {
            String popularReligionName = popularNames.get(i);
            for(int j = 0; j < allItems.size(); ++j)
            {
                Item processed = allItems.get(j);
                if(processed.value.equals(popularReligionName))
                {
                    // shuffle to start of list
                    allItems.remove(j);
                    allItems.add(0,processed);
                    break;
                }
            }
        }
        return allItems;
    }
}
