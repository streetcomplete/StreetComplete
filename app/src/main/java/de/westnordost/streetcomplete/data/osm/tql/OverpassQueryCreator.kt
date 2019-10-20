package de.westnordost.streetcomplete.data.osm.tql

class OverpassQueryCreator(
    private val elementTypes: List<String>,
    private val expr: BooleanExpression<TagFilter, Tags>?)
{
    private var setIdCounter: Int = 1
    private val dataSets: MutableMap<BooleanExpression<TagFilter, Tags>, Int> = mutableMapOf()

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
            val unionChildren =
                elementTypes.joinToString(" ") { getSetId(it, resultSetId) + ";" }
            result.append("($unionChildren);\n")
            return result.toString()
        }
    }

    private fun BooleanExpression<TagFilter, Tags>.toOverpassString(elementType: String, resultSetId: Int?): String {
        return when (this) {
            is Leaf -> AllTagFilters(value).toOverpassString(elementType, null, resultSetId)
            is AnyOf -> toOverpassString(elementType, null, resultSetId)
            is AllOf -> toOverpassString(elementType, null, resultSetId)
            else -> throw IllegalStateException("Unexpected expression")
        }
    }

    private fun AllOf<TagFilter, Tags>.childrenWithLeavesMerged(): List<BooleanExpression<TagFilter, Tags>> {
        val consecutiveLeaves = mutableListOf<TagFilter>()
        val mergedChildren = mutableListOf<BooleanExpression<TagFilter, Tags>>()
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

    private fun AllOf<TagFilter, Tags>.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
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

    private fun AnyOf<TagFilter, Tags>.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
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
        val unionChildren = childrenResultSetIds.joinToString(" ") { getSetId(elementType, it)+";" }
        val resultStmt = resultSetId?.let { " -> " + getSetId(elementType,it) }.orEmpty()
        result.append("($unionChildren)$resultStmt;\n")
        return result.toString()
    }

    private fun AllTagFilters.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        val elementFilter = elementType + inputSetId?.let { getSetId(elementType,it) }.orEmpty()
        val tagFilters = values.joinToString("") { "[${it.toOverpassQLString()}]" }
        val resultStmt = resultSetId?.let { " -> " + getSetId(elementType,it) }.orEmpty()
        return "$elementFilter$tagFilters$resultStmt;\n"
    }

    private fun getSetId(elementType: String, id: Int): String {
        val prefix = when (elementType) {
            "node" -> "n"
            "way" -> "w"
            "rel" -> "r"
            "nwr" -> "e"
            else -> throw IllegalArgumentException("Expected element to be any of 'node', 'way', 'rel' or 'nwr'")
        }
        return ".$prefix$id"
    }

    private fun BooleanExpression<TagFilter, Tags>.assignResultSetId(): Int {
        if (!dataSets.containsKey(this)) {
            dataSets[this] = setIdCounter++
        }
        return dataSets[this]!!
    }

    private class AllTagFilters(val values: List<TagFilter>) : BooleanExpression<TagFilter, Tags>() {
        constructor(value: TagFilter) : this(listOf(value))
        override fun matches(obj: Tags?) = values.all { it.matches(obj) }
        override fun toString() = values.joinToString(" and ")
    }
}
