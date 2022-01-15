package de.westnordost.streetcomplete.data

data class ColumnDefinition(
    val id: Int,
    val name: String,
    val type: String,
    val notNull: Boolean,
    val defaultValue: String?,
    val primaryKey: Int
)

fun Database.tableInfo(table: String): List<ColumnDefinition> =
    rawQuery("PRAGMA table_info($table);") { c -> c.toColumnDefinition() }

private fun CursorPosition.toColumnDefinition() = ColumnDefinition(
    getInt("cid"),
    getString("name"),
    getString("type"),
    getInt("notnull") == 1,
    getStringOrNull("dflt_value"),
    getInt("pk")
)

/** Query by multiple columns.
 *  SQLite does not support the "SELECT * FROM foo WHERE (a,b) IN ((1,2), (3,4), (5,6))" syntax,
 *  so this is a workaround. To make the query above, whereColumns would be arrayOf(a,b), whereArgs
 *  would be listOf(arrayOf(1,2),arrayOf(3,4),arrayOf(5,6)) */
fun <T> Database.queryIn(
    table: String,
    whereColumns: Array<String>,
    whereArgs: Iterable<Array<Any?>>,
    columns: Array<String>? = null,
    groupBy: String? = null,
    having: String? = null,
    orderBy: String? = null,
    limit: String? = null,
    distinct: Boolean = false,
    transform: (CursorPosition) -> T
): List<T> {
    val lookupTable = table + "_lookup"
    val lookupTableMergedView = table + "_lookup_view"

    val columnInfos = tableInfo(table).associateBy { it.name }

    val inColumnsNames = whereColumns.joinToString(",")
    val inColumnDefs = whereColumns.joinToString(",") { name ->
        val def = columnInfos.getValue(name)
        name + " " + def.type + when {
            def.notNull -> " NOT NULL"
            def.defaultValue != null -> " DEFAULT " + def.defaultValue
            else -> ""
        }
    }

    return transaction {
        /* this looks a little complicated. Basically, this is a workaround for SQLite not
           supporting the "SELECT * FROM foo WHERE (a,b) IN ((1,2), (3,4), (5,6))" syntax:
           Instead, we insert the values into a temporary table and inner join on that table then
           https://stackoverflow.com/questions/18363276/how-do-you-do-an-in-query-that-has-multiple-columns-in-sqlite
         */
        exec("""
            CREATE TEMPORARY TABLE $lookupTable(
                $inColumnDefs,
                CONSTRAINT primary_key PRIMARY KEY ($inColumnsNames)
            );"""
        )
        exec("""
            CREATE TEMPORARY VIEW $lookupTableMergedView AS
                SELECT * FROM $table INNER JOIN $lookupTable USING ($inColumnsNames)
            ;"""
        )

        insertOrIgnoreMany(lookupTable, whereColumns, whereArgs)
        val result = query(lookupTableMergedView, columns, null, null, groupBy, having, orderBy, limit, distinct, transform)
        exec("DROP VIEW $lookupTableMergedView")
        exec("DROP TABLE $lookupTable")
        result
    }
}
