package de.westnordost.streetcomplete.quests.bridge_structure;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;

public class AddBridgeStructureForm extends AbstractQuestFormAnswerFragment {

    public static final String STRUCTURE = "structure";

    private final BridgeStructure[] STRUCTURES = new BridgeStructure[] {
            new BridgeStructure("arch", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_arch),
            new BridgeStructure("beam", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_beam),
            new BridgeStructure("truss", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_truss),
            new BridgeStructure("floating", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_floating),
            new BridgeStructure("suspension", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_suspension),
            new BridgeStructure("cable-stayed", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_cablestayed),
            new BridgeStructure("simple-suspension", R.drawable.surface_asphalt, R.string.quest_bridgetructure_value_simplesuspension),
            new BridgeStructure("humpback", R.drawable.surface_asphalt, R.string.quest_bridge_structure_value_humpback)
    };

    private GroupedImageSelectAdapter imageSelector;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_bridge_structure);

        RecyclerView structureSelect = contentView.findViewById(R.id.structureSelect);
        imageSelector = new GroupedImageSelectAdapter(Arrays.<GroupedImageSelectAdapter.Item>asList(STRUCTURES));
        structureSelect.setAdapter(imageSelector);
        structureSelect.setNestedScrollingEnabled(false);

        return view;
    }

    @Override protected void onClickOk() {
        Bundle answer = new Bundle();
        if(getSelectedStructure() != null)
        {
            answer.putString(STRUCTURE, getSelectedStructure().value);
        }
        applyFormAnswer(answer);
    }

    @Override public boolean hasChanges()
    {
        return getSelectedStructure() != null;
    }

    private BridgeStructure getSelectedStructure()
    {
        return (BridgeStructure) imageSelector.getSelectedItem();
    }

    private static class BridgeStructure extends GroupedImageSelectAdapter.Item {
        public final String value;

        public BridgeStructure(String value, int drawableId, int titleId) {
            super(drawableId, titleId);
            this.value = value;
        }
    }
}
