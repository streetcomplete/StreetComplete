package de.westnordost.streetcomplete.util.serialization

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HierarchicOpeningHoursSerializer : KSerializer<HierarchicOpeningHours> {
    override val descriptor = PrimitiveSerialDescriptor(
        "de.westnordost.streetcomplete.util.serialization.HierarchicOpeningHoursSerializer",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: HierarchicOpeningHours) {
        encoder.encodeString(value.toOpeningHours().toString())
    }

    override fun deserialize(decoder: Decoder): HierarchicOpeningHours {
        return decoder.decodeString().toOpeningHours().toHierarchicOpeningHours()!!
    }
}
