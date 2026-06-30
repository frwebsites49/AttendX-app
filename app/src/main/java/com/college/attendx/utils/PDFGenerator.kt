package com.college.attendx.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.college.attendx.models.AttendanceRecord
import com.college.attendx.models.AttendanceSession
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PDFGenerator {

    fun generateAttendancePDF(
        context: Context,
        session: AttendanceSession,
        records: List<AttendanceRecord>,
        isCombined: Boolean = false
    ) {
        try {
            val fileName = if (isCombined) {
                "All_Attendance_${System.currentTimeMillis()}.pdf"
            } else {
                "${session.subject}_${session.division}_${session.group}_${System.currentTimeMillis()}.pdf"
            }

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            // Create PDF
            val writer = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            // Add Title
            val title = Paragraph(if (isCombined) "ATTENDANCE REPORT" else "ATTENDANCE SHEET")
            title.setTextAlignment(TextAlignment.CENTER)
            title.setFontSize(18f)
            title.setBold()
            document.add(title)

            document.add(Paragraph(" "))

            // Session Details
            if (!isCombined) {
                document.add(Paragraph("Subject: ${session.subject}"))
                document.add(Paragraph("Division: ${session.division}  |  Group: ${session.group}"))
                document.add(Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"))
            }

            document.add(Paragraph("Total Students Present: ${records.size}"))
            document.add(Paragraph(" "))

            // Create Table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 15f, 15f, 15f, 15f)))
            table.setWidth(UnitValue.createPercentValue(100f))

            // Headers
            val headerFont = PdfFontFactory.createFont()
            table.addCell(Cell().add(Paragraph("Roll No").setFont(headerFont).setBold()))
            table.addCell(Cell().add(Paragraph("Name").setFont(headerFont).setBold()))
            table.addCell(Cell().add(Paragraph("Division").setFont(headerFont).setBold()))
            table.addCell(Cell().add(Paragraph("Group").setFont(headerFont).setBold()))
            table.addCell(Cell().add(Paragraph("Date").setFont(headerFont).setBold()))
            table.addCell(Cell().add(Paragraph("Time").setFont(headerFont).setBold()))

            // Data
            val dataFont = PdfFontFactory.createFont()
            records.forEach { record ->
                table.addCell(Cell().add(Paragraph(record.rollNumber).setFont(dataFont)))
                table.addCell(Cell().add(Paragraph(record.studentName).setFont(dataFont)))
                table.addCell(Cell().add(Paragraph(record.division).setFont(dataFont)))
                table.addCell(Cell().add(Paragraph(record.group).setFont(dataFont)))
                table.addCell(Cell().add(Paragraph(record.date).setFont(dataFont)))
                table.addCell(Cell().add(Paragraph(record.time).setFont(dataFont)))
            }

            document.add(table)
            document.close()

            Toast.makeText(
                context,
                "✅ PDF saved: ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "❌ Error generating PDF: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }
}