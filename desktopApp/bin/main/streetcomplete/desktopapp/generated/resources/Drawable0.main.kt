@file:OptIn(InternalResourceApi::class)

package streetcomplete.desktopapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/streetcomplete.desktopapp.generated.resources/"

@delegate:ResourceContentHash(-226_326_897)
internal val Res.drawable.app_icon: DrawableResource by lazy {
      DrawableResource("drawable:app_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/app_icon.xml", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("app_icon", Res.drawable.app_icon)
}
