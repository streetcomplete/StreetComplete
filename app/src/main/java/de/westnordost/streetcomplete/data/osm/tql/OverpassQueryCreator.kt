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
            is Leaf -> listOf(value).toOverpassQLString(elementType, null, resultSetId)
            is AnyOf -> toOverpassString(elementType, null, resultSetId)
            is AllOf -> toOverpassString(elementType, null, resultSetId)
            else -> throw IllegalStateException("Unexpected expression")
        }
    }

    private fun AllOf<TagFilter, Tags>.toOverpassString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        var stmtInputSet = inputSetId
        var stmtResultSet = resultSetId
        val result = StringBuilder()

        if (children.any { it !is AnyOf && it !is Leaf })
            throw IllegalStateException("Expected only Leaf and AnyOf children")

        // need a result set if there is at least two statements. All leaves count as one only.
        val leaves = children.filterIsInstance<Leaf<TagFilter, Tags>>().map { it.value }
        val anyOfs = children.filterIsInstance<AnyOf<TagFilter, Tags>>()
        val numberOfStatements = anyOfs.size + (if (leaves.isNotEmpty()) 1 else 0)
        val needStmtResultSet = resultSetId == null && numberOfStatements >= 2

        // (...cause) all leaves are moved to the front and merged into one statement (easier code)
        if (leaves.isNotEmpty()) {
            if (needStmtResultSet) stmtResultSet = assignResultSetId()
            result.append(leaves.toOverpassQLString(elementType, stmtInputSet, stmtResultSet))
            stmtInputSet = stmtResultSet
        }

        for (anyOf in anyOfs) {
            if (needStmtResultSet) stmtResultSet = assignResultSetId()
            result.append(anyOf.toOverpassString(elementType, stmtInputSet, stmtResultSet))
            stmtInputSet = stmtResultSet
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
                    listOf(child.value).toOverpassQLString(elementType, inputSetId, workingSetId)
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

    private fun List<TagFilter>.toOverpassQLString(elementType: String, inputSetId: Int?, resultSetId: Int?): String {
        val elementFilter = elementType + inputSetId?.let { getSetId(elementType,it) }.orEmpty()
        val tagFilters = joinToString("") { "[${it.toOverpassQLString()}]" }
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
}
