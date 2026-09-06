package dev.hotest

import org.junit.Assert.assertEquals
import org.junit.Test

class ParseTableTest {

    data class SampleRow(
        val col0: String,
        val col1: String,
        val col2: Float?,
        val col3: Boolean?
    )

    @Test
    fun parseTable_passes_each_data_row_to_converter_in_order() {
        val rows = mutableListOf<List<String>>()
        val tableData = parseTable(
            """
            | col0  | col1  | col2 | col3  |
            | R0 C0 | R0 C1 | 1.23 | true  |
            | R1 C0 | R1 C1 | 2.34 |       |
            | R2 C0 | R2 C1 |      | false |
            """
        ) {
            rows.add(it)
            SampleRow(it[0], it[1], it[2].toFloatOrNull(), it[3].toBooleanStrictOrNull())
        }

        assertEquals(
            listOf(
                listOf("R0 C0", "R0 C1", "1.23", "true"),
                listOf("R1 C0", "R1 C1", "2.34", ""),
                listOf("R2 C0", "R2 C1", "", "false"),
            ),
            rows,
        )
        assertEquals(
            listOf(
                SampleRow("R0 C0", "R0 C1", 1.23f, true),
                SampleRow("R1 C0", "R1 C1", 2.34f, null),
                SampleRow("R2 C0", "R2 C1", null, false),
            ),
            tableData.rows,
        )
    }

    @Test
    fun parseTable_header_only_returns_empty_table_without_calling_converter() {
        val tableData = parseTable("| col0 |") {
            error("Converter must not be called for the header")
        }

        assertEquals(emptyList<Nothing>(), tableData.rows)
    }
}
