package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue

/** Expandable text field that can be dismissed and requests focus when it is expanded */
@Composable
fun ExpandableSearchField(
    expanded: Boolean,
    onDismiss: () -> Unit,
    search: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(expanded) {
        if (expanded) focusRequester.requestFocus()
    }
    AnimatedVisibility(visible = expanded, modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = modifier.focusRequester(focusRequester),
            leadingIcon = { SearchIcon() },
            trailingIcon = { IconButton(onClick = {
                if (search.text.isBlank()) onDismiss()
                else onSearchChange(TextFieldValue())
            }) { ClearIcon() } },
            singleLine = true,
            colors = colors
        )
    }
}
