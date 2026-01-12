package de.westnordost.streetcomplete.quests.opening_hours.ocr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.SuggestionChip

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun DayGroupingScreen(
    state: OcrFlowState,
    onStateChange: (OcrFlowState) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var selectedPreset by rememberSaveable { mutableStateOf(state.groupingPreset) }
    val customGroups = remember { mutableStateListOf<DayGroup>().apply { addAll(state.dayGroups) } }
    val selectedDays = remember { mutableStateListOf<Weekday>() }

    // Track which days are already assigned in custom mode
    val assignedDays = remember(customGroups.toList()) {
        customGroups.flatMap { it.days }.toSet()
    }

    val canContinue = when (selectedPreset) {
        DayGroupingPreset.CUSTOM -> customGroups.isNotEmpty() && assignedDays.size == Weekday.entries.size
        else -> true
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.quest_openingHours_ocr_day_grouping_title)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onBack) { BackIcon() } },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_openingHours_ocr_day_grouping_description),
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preset options
            DayGroupingPreset.entries.forEach { preset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPreset == preset,
                        onClick = {
                            selectedPreset = preset
                            if (preset != DayGroupingPreset.CUSTOM) {
                                customGroups.clear()
                                customGroups.addAll(preset.toGroups())
                            } else {
                                customGroups.clear()
                                selectedDays.clear()
                            }
                        }
                    )
                    Text(
                        text = preset.toDisplayString(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            // Custom grouping UI
            if (selectedPreset == DayGroupingPreset.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.quest_openingHours_ocr_custom_grouping_instructions),
                    style = MaterialTheme.typography.body2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Day selection chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Weekday.entries.forEach { day ->
                        val isSelected = day in selectedDays
                        val isAssigned = day in assignedDays

                        SuggestionChip(
                            onClick = {
                                if (!isAssigned) {
                                    if (isSelected) {
                                        selectedDays.remove(day)
                                    } else {
                                        selectedDays.add(day)
                                    }
                                }
                            },
                            enabled = !isAssigned,
                            colors = ChipDefaults.chipColors(
                                backgroundColor = when {
                                    isAssigned -> MaterialTheme.colors.surface.copy(alpha = 0.5f)
                                    isSelected -> MaterialTheme.colors.primary
                                    else -> MaterialTheme.colors.surface
                                },
                                contentColor = when {
                                    isAssigned -> MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                    isSelected -> MaterialTheme.colors.onPrimary
                                    else -> MaterialTheme.colors.onSurface
                                }
                            ),
                            border = if (!isAssigned && !isSelected) {
                                BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
                            } else null
                        ) {
                            Text(day.displayName)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Create group button
                OutlinedButton(
                    onClick = {
                        if (selectedDays.isNotEmpty()) {
                            customGroups.add(DayGroup(selectedDays.toList()))
                            selectedDays.clear()
                        }
                    },
                    enabled = selectedDays.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.quest_openingHours_ocr_create_group))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show created groups
                if (customGroups.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.quest_openingHours_ocr_created_groups),
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    customGroups.forEachIndexed { index, group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.toDisplayString(),
                                style = MaterialTheme.typography.body1
                            )
                            OutlinedButton(
                                onClick = { customGroups.removeAt(index) }
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_remove))
                            }
                        }
                    }
                }

                // Show remaining days hint
                val remainingDays = Weekday.entries.filter { it !in assignedDays }
                if (remainingDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.quest_openingHours_ocr_remaining_days,
                            remainingDays.joinToString(", ") { it.displayName }
                        ),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }

        // Bottom button
        Column(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    val groups = if (selectedPreset == DayGroupingPreset.CUSTOM) {
                        customGroups.toList()
                    } else {
                        selectedPreset.toGroups()
                    }

                    val annotations = groups.map { DayAnnotation(it) }

                    onStateChange(
                        state.copy(
                            groupingPreset = selectedPreset,
                            dayGroups = groups,
                            annotations = annotations,
                            currentGroupIndex = 0
                        )
                    )
                    onContinue()
                },
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.quest_openingHours_ocr_continue))
            }
        }
    }
}

@Composable
private fun DayGroupingPreset.toDisplayString(): String = when (this) {
    DayGroupingPreset.SAME_ALL_DAYS -> stringResource(R.string.quest_openingHours_ocr_preset_same_all_days)
    DayGroupingPreset.WEEKDAYS_WEEKEND -> stringResource(R.string.quest_openingHours_ocr_preset_weekdays_weekend)
    DayGroupingPreset.WEEKDAYS_SAT_SUN -> stringResource(R.string.quest_openingHours_ocr_preset_weekdays_sat_sun)
    DayGroupingPreset.CUSTOM -> stringResource(R.string.quest_openingHours_ocr_preset_custom)
}
