package com.example.mappingtest

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

/**
 * Generic Excel-based input schema used by the app.
 *
 * Recommended column names in the .xlsx file:
 * - object
 * - value
 * - date
 * - type
 * - label
 * - description
 * - status
 * - category
 * - created_at
 * - updated_at
 *
 * The parser accepts common aliases and normalizes them automatically,
 * so the sheet can be slightly different from the canonical names.
 */
data class ExcelInputRow(
    val objectName: String,
    val value: String,
    val date: String,
    val raw: Map<String, String> = emptyMap(),
)

object ExcelColumnAliases {
    val objectName = setOf("object", "obj", "entity", "field", "name")
    val value = setOf("value", "input_value", "field_value", "amount", "text")
    val date = setOf("date", "input_date", "event_date", "created_at", "updated_at")
    val type = setOf("type", "input_type", "field_type")
    val label = setOf("label", "title", "display_name")
    val description = setOf("description", "notes", "comment")
    val status = setOf("status", "state")
    val category = setOf("category", "group", "section")
    val createdAt = setOf("created_at", "createdat")
    val updatedAt = setOf("updated_at", "updatedat")
}

class ExcelInputParser {

    fun parse(inputStream: InputStream): List<ExcelInputRow> {
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val rows = mutableListOf<ExcelInputRow>()

        if (sheet.physicalNumberOfRows == 0) {
            workbook.close()
            return rows
        }

        val headerRow = sheet.getRow(0) ?: run {
            workbook.close()
            return rows
        }

        val objectIndex = findHeaderIndex(headerRow, ExcelColumnAliases.objectName)
        val valueIndex = findHeaderIndex(headerRow, ExcelColumnAliases.value)
        val dateIndex = findHeaderIndex(headerRow, ExcelColumnAliases.date)

        if (objectIndex == -1 && valueIndex == -1 && dateIndex == -1) {
            workbook.close()
            throw IllegalArgumentException(
                "Excel sheet must contain at least one of these columns: object/value/date. " +
                    "Recommended names are object, value, date."
            )
        }

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            val rawMap = readRowAsMap(headerRow, row)

            val objectName = rawMap[resolveCanonicalHeader(headerRow, ExcelColumnAliases.objectName)]
                ?: ""
            val value = rawMap[resolveCanonicalHeader(headerRow, ExcelColumnAliases.value)] ?: ""
            val date = rawMap[resolveCanonicalHeader(headerRow, ExcelColumnAliases.date)] ?: ""

            if (objectName.isBlank() && value.isBlank() && date.isBlank()) {
                continue
            }

            rows += ExcelInputRow(
                objectName = objectName.trim(),
                value = value.trim(),
                date = date.trim(),
                raw = rawMap,
            )
        }

        workbook.close()
        return rows
    }

    private fun readRowAsMap(headerRow: Row, row: Row): Map<String, String> {
        val map = linkedMapOf<String, String>()

        for (cellIndex in 0 until headerRow.lastCellNum) {
            val header = headerRow.getCell(cellIndex)?.toString()?.trim() ?: continue
            val value = row.getCell(cellIndex)?.toString()?.trim() ?: ""
            val normalizedHeader = normalizeHeader(header)
            map[normalizedHeader] = value
        }

        return map
    }

    private fun resolveCanonicalHeader(headerRow: Row, aliasSet: Set<String>): String {
        for (cellIndex in 0 until headerRow.lastCellNum) {
            val header = headerRow.getCell(cellIndex)?.toString()?.trim() ?: continue
            if (aliasSet.contains(normalizeHeader(header))) {
                return normalizeHeader(header)
            }
        }
        return ""
    }

    private fun findHeaderIndex(headerRow: Row, aliasSet: Set<String>): Int {
        for (cellIndex in 0 until headerRow.lastCellNum) {
            val header = headerRow.getCell(cellIndex)?.toString()?.trim() ?: continue
            if (aliasSet.contains(normalizeHeader(header))) {
                return cellIndex
            }
        }
        return -1
    }

    private fun normalizeHeader(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("[\s\-]+".toRegex(), "_")
            .replace("[^a-z0-9_]".toRegex(), "")
    }
}
