package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit

@Composable
fun Int.pxToDp() = with(LocalDensity.current) {
    this@pxToDp.toDp()
}

@Composable
fun Int.pxToSp() = with(LocalDensity.current) {
    this@pxToSp.toSp()
}

@Composable
fun TextUnit.toDp() = with(LocalDensity.current) {
    this@toDp.toDp()
}
