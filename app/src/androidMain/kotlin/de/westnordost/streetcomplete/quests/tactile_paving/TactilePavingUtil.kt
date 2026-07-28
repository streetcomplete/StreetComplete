package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept

// see #750, #2463, #4325, #5237
val COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON = NoCountriesExcept(
    // Europe
    "NO", "SE", "DK", "SI", "IS",
    "GB", "IE", "NL", "BE", "FR", "ES", "IT", "SM", "VA", "MC", "AD",
    "DE", "PL", "CZ", "SK", "HU", "AT", "CH", "LI",
    "LV", "LT", "EE", "LU", "RU", "HR", "PT",
    // America
    "US", "CA", "AR",
    "CO", // #5579
    // Asia
    "HK", "SG", "KR", "JP",
    // Oceania
    "AU", "NZ"
)
