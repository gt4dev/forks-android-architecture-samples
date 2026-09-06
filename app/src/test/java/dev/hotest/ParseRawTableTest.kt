package dev.hotest

import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ParseRawTableTest {

    @Test
    fun parseRawTable_happy_day_scenario() {
        val tableData = parseRawTable(
            """
            | col0  | col1  | col2  |
            | R0 C0 | R0 C1 | R0 C2 |
            | R1 C0 | R1 C1 | R1 C2 |
            | R2 C0 | R2 C1 | R2 C2 |
            """
        )

        assertTrue(tableData.rows[0][0] == "R0 C0")
        assertTrue(tableData.rows[0][1] == "R0 C1")
        assertTrue(tableData.rows[0][2] == "R0 C2")

        assertTrue(tableData.rows[1][0] == "R1 C0")
        assertTrue(tableData.rows[1][1] == "R1 C1")
        assertTrue(tableData.rows[1][2] == "R1 C2")

        assertTrue(tableData.rows[2][0] == "R2 C0")
        assertTrue(tableData.rows[2][1] == "R2 C1")
        assertTrue(tableData.rows[2][2] == "R2 C2")
    }

    @Test
    fun parseRawTable_table_is_not_pretty_printed() {
        val tableData = parseRawTable(
            """
            |col0| col1       |col2|
            | R0 C0|R0 C1 | R0 C2|
            |R1 C0       | R1 C1|R1 C2 |
            """
        )

        assertEquals(
            listOf(
                listOf("R0 C0", "R0 C1", "R0 C2"),
                listOf("R1 C0", "R1 C1", "R1 C2"),
            ),
            tableData.rows,
        )
    }

    @Test
    fun parseRawTable_empty_cells() {
        val tableData = parseRawTable(
            """
            | col0  | col1  | col2  |
            | R0 C0 |       |       |
            |       | R1 C1 |       |
            |       |       | R2 C2 |
            """
        )

        assertTrue(tableData.rows[0][0] == "R0 C0")
        assertTrue(tableData.rows[0][1] == "")
        assertTrue(tableData.rows[0][2] == "")

        assertTrue(tableData.rows[1][0] == "")
        assertTrue(tableData.rows[1][1] == "R1 C1")
        assertTrue(tableData.rows[1][2] == "")

        assertTrue(tableData.rows[2][0] == "")
        assertTrue(tableData.rows[2][1] == "")
        assertTrue(tableData.rows[2][2] == "R2 C2")
    }

    @Test
    fun parseRawTable_row_has_too_few_columns() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            parseRawTable(
                """
                | col0  | col1  | col2  |
                | R0 C0 | R0 C1 |
                """
            )
        }

        assertEquals(
            "required 3 columns (col0, col1, col2) but found 2",
            exception.message,
        )
    }

    @Test
    fun parseRawTable_row_has_too_many_columns() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            parseRawTable(
                """
                | col0  | col1  | col2  |
                | R0 C0 | R0 C1 | R0 C2 | R0 C3 |
                """
            )
        }

        assertEquals(
            "required 3 columns (col0, col1, col2) but found 4",
            exception.message,
        )
    }
}
