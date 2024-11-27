package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.QuestBuildingLevelsBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.takeFavourites
import org.koin.android.ext.android.inject
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.isDigitsOnly
import de.westnordost.streetcomplete.util.ktx.conditional

@Composable
fun AddBuildingLevelsFormControl(
    regularLevels: MutableState<String>,
    roofLevels: MutableState<String>,
    modifier: Modifier = Modifier,
    buildingLevels: List<BuildingLevelsAnswer> = listOf(),
    onFormChanged: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    Box(modifier = modifier) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier
                    .padding(3.dp)
                    .weight(1f)) {
                    Text(
                        stringResource(R.string.quest_buildingLevels_levelsLabel2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(0.dp, 12.dp)
                    )
                    TextField(
                        regularLevels.value,
                        onValueChange = {
                            regularLevels.value = it
                            onFormChanged()
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .padding(vertical = 9.dp)
                            .conditional(regularLevels.value == "") { focusRequester(focusRequester) }
                            .conditional(!regularLevels.value.isDigitsOnly()) { border(2.dp, color = Color.Red)},
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Start,
                            fontSize = 20.sp
                        ),

                        )
                }
                Image(
                    painter = painterResource(R.drawable.building_levels_illustration),
                    contentDescription = "Illustration for building Levels",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .defaultMinSize()
                        .width(239.dp)
                        .weight(2f)
                        .padding(3.dp)
                )
                Column(modifier = Modifier
                    .padding(3.dp)
                    .weight(1f)) {
                    TextField(
                        roofLevels.value, onValueChange = {
                            roofLevels.value = it
                            onFormChanged()
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .padding(0.dp, 12.dp)
                            .align(Alignment.CenterHorizontally)
                            .conditional(regularLevels.value != "") { focusRequester(focusRequester) }
                            .conditional(!roofLevels.value.isDigitsOnly()) { border(2.dp, color = Color.Red)},
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Start,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.quest_buildingLevels_roofLevelsLabel2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(0.dp, 12.dp)
                    )
                }
            }
            LazyRow(modifier = Modifier.defaultMinSize(minHeight = 52.dp)) {
                items(buildingLevels.size) { position ->
                    val curLevel = buildingLevels[position]
                    AddBuildingLevelsButton(
                        curLevel.levels,
                        curLevel.roofLevels,
                        modifier = Modifier.clickable(onClick = {
                            regularLevels.value = curLevel.levels.toString()
                            roofLevels.value = curLevel.roofLevels.toString()
                            onFormChanged()

                        })
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
@Preview(showBackground = true, name = "Add Building Levels Form Component")
fun PreviewAddBuildingLevelsFormControl() {
    var regularLevels = remember { mutableStateOf("55") }
    var roofLevels = remember { mutableStateOf("55") }
    AddBuildingLevelsFormControl(
        regularLevels,
        roofLevels,
        buildingLevels = listOf(
            BuildingLevelsAnswer(5, 2),
            BuildingLevelsAnswer(4, 1),
            BuildingLevelsAnswer(3, 0)
        )
    )
}

class AddBuildingLevelsForm : AbstractOsmQuestForm<BuildingLevelsAnswer>() {

    override val contentLayoutResId = R.layout.quest_building_levels
    private val binding by contentViewBinding(QuestBuildingLevelsBinding::bind)

    private val prefs: Preferences by inject()
    private lateinit var regularLevels: MutableState<String>
    private lateinit var roofLevels: MutableState<String>
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingLevels_answer_multipleLevels) { showMultipleLevelsHint() }
    )

    private val lastPickedAnswers by lazy {
        prefs.getLastPicked(this::class.simpleName!!)
            .map { value ->
                value.split("#")
                    .let { BuildingLevelsAnswer(it[0].toInt(), it.getOrNull(1)?.toInt()) }
            }
            .takeFavourites(n = 5, history = 15, first = 1)
            .sortedWith(compareBy<BuildingLevelsAnswer> { it.levels }.thenBy { it.roofLevels })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questBuildingLevelsBase.setContent {
            regularLevels = rememberSaveable { mutableStateOf(if(element.tags["building:levels"]!=null) element.tags["building:levels"].toString() else "") }
            roofLevels = rememberSaveable { mutableStateOf(if(element.tags["roof:levels"]!=null) element.tags["roof:levels"].toString() else "") }
            AddBuildingLevelsFormControl(
                regularLevels,
                roofLevels,
                onFormChanged = { checkIsFormComplete() },
                buildingLevels = lastPickedAnswers
            )
        }
    }

    override fun onClickOk() {
        val answer = BuildingLevelsAnswer(
            regularLevels.value.toInt(),
            if (roofLevels.value != "") roofLevels.value.toInt() else null
        )
        prefs.addLastPicked(
            this::class.simpleName!!,
            listOfNotNull(answer.levels, answer.roofLevels).joinToString("#")
        )
        applyAnswer(answer)
    }

    private fun showMultipleLevelsHint() {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.quest_buildingLevels_answer_description)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    override fun isFormComplete(): Boolean {
        val hasNonFlatRoofShape =
            element.tags.containsKey("roof:shape") && element.tags["roof:shape"] != "flat"
        val roofLevelsAreOptional = countryInfo.roofsAreUsuallyFlat && !hasNonFlatRoofShape
        return regularLevels.value != ""
            && regularLevels.value.isDigitsOnly()
            && regularLevels.value.toInt() >= 0
            && (roofLevelsAreOptional || (roofLevels.value != "" && roofLevels.value.isDigitsOnly() && roofLevels.value.toInt()>=0))
        return false
    }
}
