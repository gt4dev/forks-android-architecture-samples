package dev.hotest


// todo: dorobic tez wariant z Map, bo bezpieczniejszy np. myMap['colA'] i jak U zmieni tabele to sie nie posypie
// - fun <MyRow> parseTable(table: String, converter: (Map<String,String>) -> MyRow): Table<MyRow> {
// - i wtedy `converter: (List<String>) ..` to special case of Map

fun <TRow> parseTable(table: String, converterRow: (List<String>) -> TRow): Table<TRow> {
    val tableData = parseTableData(table)
    TODO()
}

fun parseTableData(table: String): RawTable {
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
