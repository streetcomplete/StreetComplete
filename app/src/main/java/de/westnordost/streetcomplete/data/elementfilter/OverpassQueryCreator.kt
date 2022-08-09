package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.RELATIONS
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.data.elementfilter.filters.toOverpassString
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** Create an overpass query from the given element filter expression */
class OverpassQueryCreator(
    elementTypes: Set<ElementsTypeFilter>,
    private val expr: BooleanExpression<ElementFilter, Element>?
) {
    private val elementTypes = elementTypes.toOqlNames()
    private var setIdCounter: Int = 1
    private val dataSets: MutableMap<BooleanExpression<ElementFilter, Element>, Int> = mutableMapOf()

    fun create(): String {
        if (elementTypes.size == 1) {
            val elementType = elementTypes.first()
            if (expr == null) {
                return "$elementType;\n"
            }
            return expr.toOverpassString(elementType, null)
        } else {
            if (expr == null) {
                return "(" + elementTypes.joinToString(" ") { "$it; " } + ");\n"
            }

            val result = StringBuilder()
            val resultSetId = expr.assignResultSetId()
            for (elementType in elementTypes) {
                result.append(expr.toOverpassString(elementType, resultSetId))
            }
            val unionChildren = elementTypes.joinToString(" ") { getSetId(it, resultSetId) + ";" }
            result.append("($unionChildren);\n")
            return result.toString()
        }
    }

    private fun Set<ElementsTypeFilter>.toOqlNames(): List<String> = when {
        containsAll(listOf(NODES, WAYS, RELATIONS)) ->  listOf("nwr")
        containsAll(listOf(NODES, WAYS)) ->             listOf("nw")
        containsAll(listOf(WAYS, RELATIONS)) ->         listOf("wr")
        else -> map { when (it) {
            NODES -> "node"
            WAYS -> "way"
            RELATIONS -> "rel"
        } }
    }

    private fun BooleanExpression<ElementFilter, Element>.toOverpassString(elementType: String, resultSetId: Int?): String {
        return when (this) {
            is Leaf -> AllTagFilters(value).toOverpassString(elementType, null, resultSetId)
            is AnyOf -> toOverpassString(elementType, null, resultSetId)
            is AllOf -> toOverpassString(elementType, null, resultSetId)
            else -> throw IllegalStateException("Unexpected expression")
        }
    }

    private fun AllOf<ElementFilter, Element>.childrenWithLeavesMerged(): List<BooleanExpression<ElementFilter, Element>> {
        val consecutiveLeaves = mutableListOf<ElementFilter>()
        val mergedChildren = mutableListOf<BooleanExpression<ElementFilter, Element>>()
        for (child in children) {
            when (child) {
                is Leaf -> consecutiveLeaves.add(child.value)
                is AnyOf -> {
                    if (consecutiveLeaves.isNotEmpty()) {
                        mergedChildren.add(AllTagFilters(consecutiveLeaves.toList()))
                        consecutiveLeaves.clear()
                    }
                    mergedChildren.add(child)
                }
                else -> throw IllegalStateException("Expected only Leaf and AnyOf children")
            }
        }
        if (consecutiveLeaves.isNotEmpty()) {
            mergedChildren.add(AllTagFilters(consecutiveLeaves.toList()))
        }
        return mergedChildren
    }

    private fun AllOf<ElementFilter, Element>.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        val result = StringBuilder()
        val workingSet by lazy { assignResultSetId() }

        val childrenMerged = childrenWithLeavesMerged()
        childrenMerged.forEachIndexed { i, child ->
            val isFirst = i == 0
            val isLast = i == childrenMerged.lastIndex
            val stmtInputSetId = if (isFirst) inputSetId else workingSet
            val stmtResultSetId = if (isLast) resultSetId else workingSet

            if (child is AnyOf) result.append(child.toOverpassString(elementType, stmtInputSetId, stmtResultSetId))
            else if (child is AllTagFilters) result.append(child.toOverpassString(elementType, stmtInputSetId, stmtResultSetId))
        }
        return result.toString()
    }

    private fun AnyOf<ElementFilter, Element>.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        val childrenResultSetIds = mutableListOf<Int>()
        val result = StringBuilder()
        // first print every nested statement
        for (child in children) {
            val workingSetId = child.assignResultSetId()
            result.append(when (child) {
                is Leaf ->
                    AllTagFilters(child.value).toOverpassString(elementType, inputSetId, workingSetId)
                is AllOf ->
                    child.toOverpassString(elementType, inputSetId, workingSetId)
                else ->
                    throw IllegalStateException("Expected only Leaf and AllOf children")
            })
            childrenResultSetIds.add(workingSetId)
        }
        // then union all direct children
        val unionChildren = childrenResultSetIds.joinToString(" ") { getSetId(elementType, it) + ";" }
        val resultStmt = resultSetId?.let { " -> " + getSetId(elementType, it) }.orEmpty()
        result.append("($unionChildren)$resultStmt;\n")
        return result.toString()
    }

    private fun AllTagFilters.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        val elementFilter = elementType + inputSetId?.let { getSetId(elementType, it) }.orEmpty()
        val tagFilters = values.joinToString("") { it.toOverpassString() }
        val resultStmt = resultSetId?.let { " -> " + getSetId(elementType, it) }.orEmpty()
        return "$elementFilter$tagFilters$resultStmt;\n"
    }

    private fun getSetId(elementType: String, id: Int): String {
        val prefix = when (elementType) {
            "node" -> "n"
            "way" -> "w"
            "rel" -> "r"
            "nwr", "nw", "wr" -> "e"
            else -> throw IllegalArgumentException("Expected element to be any of 'node', 'way', 'rel', 'nw', 'wr' or 'nwr'")
        }
        return ".$prefix$id"
    }

    private fun BooleanExpression<ElementFilter, Element>.assignResultSetId(): Int {
        return dataSets.getOrPut(this) { setIdCounter++ }
    }

    private class AllTagFilters(val values: List<ElementFilter>) : BooleanExpression<ElementFilter, Element>() {
        constructor(value: ElementFilter) : this(listOf(value))
        override fun matches(obj: Element) = values.all { it.matches(obj) }
        override fun toString() = values.joinToString(" and ")
    }
}

/** @return this expression as a Overpass query string */
fun ElementFilterExpression.toOverpassQLString(): String =
    OverpassQueryCreator(elementsTypes, elementExprRoot).create()
