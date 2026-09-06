package dev.hotest



fun <TRow> parseTable(table: String, rowConverter: (List<String>) -> TRow): Table<TRow> {
    val rawTable = parseRawTable(table)
    return Table(rawTable.rows.map(rowConverter))
}

fun parseRawTable(table: String): RawTable {
    val tableRows = table
        .lineSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
            require(line.startsWith("|") && line.endsWith("|")) {
                "Table row must start and end with |: $line"
            }

            line
                .removePrefix("|")
                .removeSuffix("|")
                .split("|")
                .map(String::trim)
        }
        .toList()

    require(tableRows.isNotEmpty()) { "Table must contain a header row" }

    val header = tableRows.first()
    val rows = tableRows.drop(1)

    rows.forEach { row ->
        require(row.size == header.size) {
            "required ${header.size} columns (${header.joinToString()}) but found ${row.size}"
        }
    }

    return RawTable(rows)
}

class RawTable(
    val rows: List<List<String>>
)

class Table<T>(
    val rows: List<T>
)
