package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
@ReadOnlyComposable
fun Int.pxToDp() = with(LocalDensity.current) {
    this@pxToDp.toDp()
}

@Composable
@ReadOnlyComposable
fun Int.pxToSp() = with(LocalDensity.current) {
    this@pxToSp.toSp()
}

@Composable
@ReadOnlyComposable
fun TextUnit.toDp() = with(LocalDensity.current) {
    this@toDp.toDp()
}

@Composable
@ReadOnlyComposable
fun Dp.dpToSp() = with(LocalDensity.current) {
    this@dpToSp.toSp()
}
