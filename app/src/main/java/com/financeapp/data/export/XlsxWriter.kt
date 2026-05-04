package com.financeapp.data.export

import com.financeapp.domain.model.Transaction
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxWriter {
    fun createXlsx(transactions: List<Transaction>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            // [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>""".toByteArray())
            zip.closeEntry()

            // _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".toByteArray())
            zip.closeEntry()

            // xl/workbook.xml
            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Transactions" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""".toByteArray())
            zip.closeEntry()

            // xl/_rels/workbook.xml.rels
            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>""".toByteArray())
            zip.closeEntry()

            // Build shared strings
            val strings = mutableListOf<String>()
            fun strIndex(s: String): Int {
                val idx = strings.indexOf(s)
                return if (idx >= 0) idx else { strings.add(s); strings.size - 1 }
            }

            val headers = listOf("Date", "Type", "Amount", "Fee", "Points", "Account", "To Account", "Category", "Note", "Currency")
            headers.forEach { strIndex(it) }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val rows = transactions.map { t ->
                listOf(
                    sdf.format(Date(t.date)),
                    t.type.name,
                    String.format("%.2f", t.amount),
                    String.format("%.2f", t.fee),
                    String.format("%.0f", t.points),
                    t.accountName,
                    t.toAccountName,
                    t.categoryName,
                    t.note,
                    t.currency
                )
            }

            // xl/worksheets/sheet1.xml
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            val sb = StringBuilder()
            sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>""")
            // Header row
            sb.append("<row r=\"1\">")
            headers.forEachIndexed { col, h ->
                val colLetter = ('A' + col)
                sb.append("<c r=\"$colLetter${1}\" t=\"s\"><v>${strIndex(h)}</v></c>")
            }
            sb.append("</row>")
            // Data rows
            rows.forEachIndexed { rowIdx, cols ->
                val rowNum = rowIdx + 2
                sb.append("<row r=\"$rowNum\">")
                cols.forEachIndexed { col, value ->
                    val colLetter = ('A' + col)
                    val cellRef = "$colLetter$rowNum"
                    val numVal = value.toDoubleOrNull()
                    if (numVal != null && col in 2..4) {
                        sb.append("<c r=\"$cellRef\"><v>$value</v></c>")
                    } else {
                        sb.append("<c r=\"$cellRef\" t=\"s\"><v>${strIndex(value)}</v></c>")
                    }
                }
                sb.append("</row>")
            }
            sb.append("</sheetData></worksheet>")
            zip.write(sb.toString().toByteArray())
            zip.closeEntry()

            // xl/sharedStrings.xml
            zip.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            val ssb = StringBuilder()
            ssb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">""")
            strings.forEach { s ->
                ssb.append("<si><t>${s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")}</t></si>")
            }
            ssb.append("</sst>")
            zip.write(ssb.toString().toByteArray())
            zip.closeEntry()
        }
        return baos.toByteArray()
    }
}
