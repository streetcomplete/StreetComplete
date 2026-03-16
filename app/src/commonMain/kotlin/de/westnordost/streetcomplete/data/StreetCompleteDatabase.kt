package de.westnordost.streetcomplete.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.execSQL
import de.westnordost.streetcomplete.data.ConflictAlgorithm.ABORT
import de.westnordost.streetcomplete.data.ConflictAlgorithm.FAIL
import de.westnordost.streetcomplete.data.ConflictAlgorithm.IGNORE
import de.westnordost.streetcomplete.data.ConflictAlgorithm.REPLACE
import de.westnordost.streetcomplete.data.ConflictAlgorithm.ROLLBACK
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

class StreetCompleteDatabase(private val databaseConnection: SQLiteConnection) : Database {
    private val lock = ReentrantLock()
    private var transactionDepth = 0

    init {
        val oldVersion = databaseConnection.prepare("PRAGMA user_version").use { statement ->
            statement.toSequence { it.getInt("user_version") }.single()
        }
        val newVersion = DatabaseInitializer.DB_VERSION

        if (oldVersion == 0) {
            DatabaseInitializer.onCreate(this)
        } else if (oldVersion < newVersion) {
            DatabaseInitializer.onUpgrade(this, oldVersion, newVersion)
            databaseConnection.execSQL("PRAGMA user_version = $newVersion")
        }
    }

    override fun exec(sql: String, args: Array<Any>?): Unit = lock.withLock {
        databaseConnection.prepare(sql).use { statement ->
            statement.bindAll(args)
            statement.step()
        }
    }

    override fun <T> rawQuery(
        sql: String,
        args: Array<Any>?,
        transform: (CursorPosition) -> T,
    ): List<T> = lock.withLock {
        databaseConnection.prepare(sql).use { statement ->
            statement.bindAll(args)
            statement.toSequence(transform).toList()
        }
    }

    override fun <T> queryOne(
        table: String,
        columns: Array<String>?,
        where: String?,
        args: Array<Any>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        transform: (CursorPosition) -> T,
    ): T? = lock.withLock {
        databaseConnection.prepareQuery(false, table, columns, where, groupBy, having, orderBy, 1).use { statement ->
            statement.bindAll(args)
            statement.toSequence(transform).firstOrNull()
        }
    }

    override fun <T> query(
        table: String,
        columns: Array<String>?,
        where: String?,
        args: Array<Any>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: Int?,
        distinct: Boolean,
        transform: (CursorPosition) -> T,
    ): List<T> = lock.withLock {
        databaseConnection.prepareQuery(distinct, table, columns, where, groupBy, having, orderBy, limit).use { statement ->
            statement.bindAll(args)
            statement.toSequence(transform).toList()
        }
    }

    override fun insert(
        table: String,
        values: Collection<Pair<String, Any?>>,
        conflictAlgorithm: ConflictAlgorithm?,
    ): Long = lock.withLock {
        databaseConnection.prepareInsert(table, values.map { it.first }, conflictAlgorithm).use { statement ->
            statement.bindAll(values.map { it.second }.toTypedArray())
            statement.executeInsert()
        }
    }

    override fun insertMany(
        table: String,
        columnNames: Array<String>,
        valuesList: Iterable<Array<Any?>>,
        conflictAlgorithm: ConflictAlgorithm?,
    ): List<Long> = lock.withLock {
        databaseConnection.prepareInsert(table, columnNames.toList(), conflictAlgorithm).use { statement ->
            val result = ArrayList<Long>()
            transaction {
                for (values in valuesList) {
                    require(values.size == columnNames.size)
                    statement.bindAll(values)
                    val rowId = statement.executeInsert()
                    result.add(rowId)
                    statement.clearBindings()
                    statement.reset()
                }
            }
            result
        }
    }

    override fun update(
        table: String,
        values: Collection<Pair<String, Any?>>,
        where: String?,
        args: Array<Any>?,
        conflictAlgorithm: ConflictAlgorithm?,
    ): Int = lock.withLock {
        databaseConnection.prepareUpdate(table, values, where, conflictAlgorithm).use { statement ->
            statement.bindAll(args)
            statement.toSequence { }.count()
        }
    }

    override fun delete(table: String, where: String?, args: Array<Any>?): Int = lock.withLock {
        databaseConnection.prepareDelete(table, where).use { statement ->
            statement.bindAll(args)
            statement.toSequence { }.count()
        }
    }

    override fun <T> transaction(block: () -> T): T = lock.withLock {
        val isOutermost = transactionDepth == 0
        transactionDepth++
        try {
            if (isOutermost) {
                databaseConnection.execSQL("BEGIN IMMEDIATE TRANSACTION")
            }
            val result = block()
            if (isOutermost) {
                databaseConnection.execSQL("END TRANSACTION")
            }
            return result
        } catch (t: Throwable) {
            if (isOutermost) {
                databaseConnection.execSQL("ROLLBACK TRANSACTION")
            }
            throw t
        } finally {
            transactionDepth--
        }
    }
}

class SQLiteCursorPosition(private val statement: SQLiteStatement) : CursorPosition {
    override fun getInt(columnName: String): Int = statement.getInt(index(columnName))
    override fun getLong(columnName: String): Long = statement.getLong(index(columnName))
    override fun getDouble(columnName: String): Double = statement.getDouble(index(columnName))
    override fun getFloat(columnName: String): Float = statement.getFloat(index(columnName))
    override fun getBlob(columnName: String): ByteArray = statement.getBlob(index(columnName))
    override fun getString(columnName: String): String = statement.getText(index(columnName))
    override fun getIntOrNull(columnName: String): Int? = statement.getIntOrNull(index(columnName))
    override fun getLongOrNull(columnName: String): Long? = statement.getLongOrNull(index(columnName))
    override fun getDoubleOrNull(columnName: String): Double? = statement.getDoubleOrNull(index(columnName))
    override fun getFloatOrNull(columnName: String): Float? = statement.getFloatOrNull(index(columnName))
    override fun getBlobOrNull(columnName: String): ByteArray? = statement.getBlobOrNull(index(columnName))
    override fun getStringOrNull(columnName: String): String? = statement.getTextOrNull(index(columnName))

    private val columnIndices: Map<String, Int> by lazy {
        statement.getColumnNames().withIndex().associate { (i, name) -> name to i }
    }

    private fun index(columnName: String): Int =
        columnIndices[columnName] ?: throw IllegalArgumentException("Column $columnName not found")
}

private fun ConflictAlgorithm?.toSql() = when (this) {
    ROLLBACK -> "OR ROLLBACK"
    ABORT -> "OR ABORT"
    FAIL -> "OR FAIL"
    IGNORE -> "OR IGNORE"
    REPLACE -> "OR REPLACE"
    null -> ""
}

private fun SQLiteConnection.prepareQuery(
    distinct: Boolean,
    table: String,
    columns: Array<String>?,
    where: String?,
    groupBy: String?,
    having: String?,
    orderBy: String?,
    limit: Int?,
): SQLiteStatement {
    require (having.isNullOrBlank() || !groupBy.isNullOrBlank()) {
        "`having` clauses are only permitted when using a `groupBy clause"
    }

    val columnNames = if (columns.isNullOrEmpty()) "*" else columns.joinToString(", ")
    val sql = StringBuilder("SELECT")
    if (distinct) sql.append(" DISTINCT")
    sql.append(" $columnNames FROM $table")
    where?.let { sql.append(" WHERE $it") }
    groupBy?.let { sql.append(" GROUP BY $it") }
    having?.let { sql.append(" HAVING $it") }
    orderBy?.let { sql.append(" ORDER BY $it") }
    limit?.let { sql.append(" LIMIT $it") }

    return prepare(sql.toString())
}

private fun getPlaceholdersString(count: Int) =
    if (count == 0) "NULL" else Array(count) { "?" }.joinToString(", ")

private fun SQLiteConnection.prepareInsert(
    table: String,
    columns: List<String>,
    conflictAlgorithm: ConflictAlgorithm?,
): SQLiteStatement {
    val conflictSql = conflictAlgorithm.toSql()
    val columnNames = columns.joinToString(", ")
    val placeholders = getPlaceholdersString(columns.size)
    val sql = "INSERT $conflictSql INTO $table ($columnNames) VALUES ($placeholders) RETURNING ROWID"

    return prepare(sql)
}

private fun SQLiteConnection.prepareUpdate(
    table: String,
    values: Collection<Pair<String, Any?>>,
    where: String?,
    conflictAlgorithm: ConflictAlgorithm?,
): SQLiteStatement {
    val conflictSql = conflictAlgorithm.toSql()
    val placeholders = values.map { it.first + "=?" }
    val whereClause = if (where.isNullOrBlank()) "" else "WHERE $where"
    val sql = "UPDATE $conflictSql $table SET $placeholders $whereClause RETURNING 1"

    return prepare(sql)
}

private fun SQLiteConnection.prepareDelete(
    table: String,
    where: String?,
): SQLiteStatement {
    val whereClause = if (where.isNullOrBlank()) "" else "WHERE $where"
    val sql = "DELETE FROM $table $whereClause RETURNING 1"

    return prepare(sql)
}

private fun SQLiteStatement.bind(i: Int, value: Any?) {
    when (value) {
        null -> bindNull(i)
        is String -> bindText(i, value)
        is Double -> bindDouble(i, value)
        is Long -> bindLong(i, value)
        is ByteArray -> bindBlob(i, value)
        is Int -> bindInt(i, value)
        is Float -> bindFloat(i, value)
        else -> {
            val valueType = value.javaClass.canonicalName
            throw IllegalArgumentException("Illegal value type $valueType at column $i")
        }
    }
}

private fun <T> SQLiteStatement.bindAll(args: Array<T>?) {
    if (args != null) {
        for ((index, value) in args.withIndex()) {
            bind(index + 1, value)
        }
    }
}

private fun <T> SQLiteStatement.toSequence(transform: (CursorPosition) -> T): Sequence<T> {
    val c = SQLiteCursorPosition(this)
    return sequence {
        while (step()) {
            yield(transform(c))
        }
    }
}
private fun SQLiteStatement.executeInsert(): Long =
    toSequence { it.getLong("rowid") }.single()

private fun SQLiteStatement.getIntOrNull(index: Int) = if (isNull(index)) null else getInt(index)
private fun SQLiteStatement.getLongOrNull(index: Int) = if (isNull(index)) null else getLong(index)
private fun SQLiteStatement.getDoubleOrNull(index: Int) = if (isNull(index)) null else getDouble(index)
private fun SQLiteStatement.getFloatOrNull(index: Int) = if (isNull(index)) null else getFloat(index)
private fun SQLiteStatement.getBlobOrNull(index: Int) = if (isNull(index)) null else getBlob(index)
private fun SQLiteStatement.getTextOrNull(index: Int) = if (isNull(index)) null else getText(index)
