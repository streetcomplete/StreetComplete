package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser

@Composable
fun rememberPath(string: String): List<PathNode> =
    remember { PathParser().parsePathString(string).toNodes() }
