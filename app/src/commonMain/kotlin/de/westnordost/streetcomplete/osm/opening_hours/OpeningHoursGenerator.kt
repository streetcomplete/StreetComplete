package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.RuleOperator
import de.westnordost.osm_opening_hours.model.RuleType

fun HierarchicOpeningHours.toOpeningHours(): OpeningHours {
    val rules = mutableListOf<Rule>()
    for (months in monthsList) {
        for (weekdays in months.weekdaysList) {
            val monthsSelectors = months.selectors.takeIf { it.isNotEmpty() }
            val weekdaysSelectors = weekdays.weekdaysSelectors.takeIf { it.isNotEmpty() }
            val holidaysSelectors = weekdays.holidaysSelectors.takeIf { it.isNotEmpty() }
            val rule = when (weekdays.times) {
                Off -> Rule(
                    selector = Range(
                        months = monthsSelectors,
                        weekdays = weekdaysSelectors,
                        holidays = holidaysSelectors,
                    ),
                    ruleType = RuleType.Off
                )
                is Times -> Rule(
                    selector = Range(
                        months = monthsSelectors,
                        weekdays = weekdaysSelectors,
                        holidays = holidaysSelectors,
                        times = weekdays.times.selectors
                    ),
                )
            }
            rules.add(rule)
        }
    }

    return OpeningHours(rules.offRulesMovedToBack().asNonColliding())
}

/* if any rule collides with another, e.g. "Mo-Fr 10:00-12:00; We 14:00-16:00", switch to
   additive rules e.g. "Mo-Fr 10:00-12:00, We 14:00-16:00" */
private fun List<Rule>.asNonColliding(): List<Rule> =
    if (!hasCollidingWeekdays()) {
        this
    } else {
        map { rule ->
            // "off" rules stay non-additive
            if (rule.ruleType == RuleType.Off) {
                rule
            } else {
                rule.copy(ruleOperator = RuleOperator.Additional)
            }
        }
    }

/** move all off-rules to the back, so normal rules don't overwrite these */
private fun List<Rule>.offRulesMovedToBack(): List<Rule> =
    if (none { it.ruleType == RuleType.Off }) {
        this
    } else {
        val (normalRules, offRules) = partition { it.ruleType != RuleType.Off }
        normalRules + offRules
    }
