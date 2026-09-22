package com.bahri.lovelypos.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.bahri.lovelypos.domain.model.SummaryReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfFileResult(
    val uri: Uri,
    val file: File,
    val fileName: String
)

object PdfReportGenerator {

    fun generateSummaryPdfResult(
        context: Context,
        report: SummaryReport,
        dateRangeLabel: String
    ): PdfFileResult? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points (72 dpi)
        val pageHeight = 842 // A4 height in points
        val margin = 36f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintPrimary = Paint().apply {
            color = Color.parseColor("#1B5E20") // Dark Green theme
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintSecondary = Paint().apply {
            color = Color.parseColor("#455A64")
            textSize = 10f
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = Color.parseColor("#CFD8DC")
            strokeWidth = 1f
            isAntiAlias = true
        }

        val paintTableHeadBg = Paint().apply {
            color = Color.parseColor("#E8F5E9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var y = margin

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > pageHeight - margin - 30) {
                // Draw footer on current page
                val paintFooter = Paint().apply {
                    color = Color.GRAY
                    textSize = 8f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    isAntiAlias = true
                }
                canvas.drawText("Halaman $pageNumber | LovelyPOS Lite", margin, pageHeight - 20f, paintFooter)

                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = margin
            }
        }

        // --- HEADER ---
        canvas.drawText("Laporan Penjualan LovelyPOS", margin, y + 16, paintPrimary)
        y += 24f

        val timeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
        canvas.drawText("Periode: $dateRangeLabel", margin, y + 10, paintBold)
        canvas.drawText("Dicetak: $timeStamp", pageWidth - margin - 150f, y + 10, paintSecondary)
        y += 20f

        canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
        y += 16f

        // --- SECTION 1: RINGKASAN UTAMA ---
        checkPageBreak(80f)
        canvas.drawText("Ringkasan Penjualan", margin, y + 12, paintBold.apply { textSize = 12f })
        y += 20f

        val totalRevText = CurrencyFormatter.formatRupiahWithoutDecimal(report.totalRevenue)
        val avgRev = if (report.transactionCount > 0) report.totalRevenue / report.transactionCount else 0L
        val avgRevText = CurrencyFormatter.formatRupiahWithoutDecimal(avgRev)

        val cardWidth = (pageWidth - (margin * 2) - 16) / 3
        drawSummaryBox(canvas, margin, y, cardWidth, 50f, "Total Pendapatan", totalRevText, paintTableHeadBg, paintLine, paintSecondary, paintBold)
        drawSummaryBox(canvas, margin + cardWidth + 8, y, cardWidth, 50f, "Jumlah Transaksi", "${report.transactionCount} Transaksi", paintTableHeadBg, paintLine, paintSecondary, paintBold)
        drawSummaryBox(canvas, margin + (cardWidth * 2) + 16, y, cardWidth, 50f, "Rata-rata / Transaksi", avgRevText, paintTableHeadBg, paintLine, paintSecondary, paintBold)
        y += 64f

        // Reset bold paint textSize
        paintBold.textSize = 10f

        // Standard column alignment coordinates for all tables
        val col1X = margin
        val col2X = margin + 220f
        val col3X = pageWidth - margin - 120f

        // --- SECTION 2: BREAKDOWN METODE PEMBAYARAN ---
        if (report.paymentBreakdown.isNotEmpty()) {
            checkPageBreak(60f)
            canvas.drawText("Rincian Metode Pembayaran", margin, y + 12, paintBold.apply { textSize = 12f })
            y += 20f

            paintBold.textSize = 10f

            // Table Header
            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, paintTableHeadBg)
            canvas.drawText("Metode Pembayaran", col1X + 8, y + 14, paintBold)
            canvas.drawText("Jumlah", col2X, y + 14, paintBold)
            canvas.drawText("Total", col3X, y + 14, paintBold)
            y += 20f

            report.paymentBreakdown.forEach { pm ->
                checkPageBreak(20f)
                canvas.drawText(pm.paymentMethod, col1X + 8, y + 14, paintText)
                canvas.drawText("${pm.count}x", col2X, y + 14, paintText)
                canvas.drawText(CurrencyFormatter.formatRupiahWithoutDecimal(pm.total), col3X, y + 14, paintText)
                y += 18f
                canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
            }
            y += 16f
        }

        // --- SECTION 3: PRODUK TERLARIS ---
        if (report.topItems.isNotEmpty()) {
            checkPageBreak(60f)
            canvas.drawText("Produk Terlaris", margin, y + 12, paintBold.apply { textSize = 12f })
            y += 20f

            paintBold.textSize = 10f

            // Table Header
            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, paintTableHeadBg)
            canvas.drawText("Nama Produk", col1X + 8, y + 14, paintBold)
            canvas.drawText("Qty Terjual", col2X, y + 14, paintBold)
            canvas.drawText("Total Omset", col3X, y + 14, paintBold)
            y += 20f

            report.topItems.forEach { item ->
                checkPageBreak(20f)
                val truncatedName = if (item.menuItemName.length > 30) item.menuItemName.take(27) + "..." else item.menuItemName
                canvas.drawText(truncatedName, col1X + 8, y + 14, paintText)
                canvas.drawText("${item.totalQty}", col2X, y + 14, paintText)
                canvas.drawText(CurrencyFormatter.formatRupiahWithoutDecimal(item.totalRevenue), col3X, y + 14, paintText)
                y += 18f
                canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
            }
            y += 16f
        }

        // --- SECTION 4: PENJUALAN HARIAN ---
        if (report.dailyRevenue.isNotEmpty()) {
            checkPageBreak(60f)
            canvas.drawText("Rincian Penjualan Harian", margin, y + 12, paintBold.apply { textSize = 12f })
            y += 20f

            paintBold.textSize = 10f

            // Table Header
            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, paintTableHeadBg)
            canvas.drawText("Tanggal", col1X + 8, y + 14, paintBold)
            canvas.drawText("Transaksi", col2X, y + 14, paintBold)
            canvas.drawText("Total", col3X, y + 14, paintBold)
            y += 20f

            report.dailyRevenue.forEach { daily ->
                checkPageBreak(20f)
                canvas.drawText(daily.dateLabel, col1X + 8, y + 14, paintText)
                canvas.drawText("${daily.count}x", col2X, y + 14, paintText)
                canvas.drawText(CurrencyFormatter.formatRupiahWithoutDecimal(daily.total), col3X, y + 14, paintText)
                y += 18f
                canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
            }
            y += 16f
        }

        // --- FOOTER ON LAST PAGE ---
        val paintFooter = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Halaman $pageNumber | LovelyPOS Lite", margin, pageHeight - 20f, paintFooter)

        pdfDocument.finishPage(page)

        // Save file to cache/reports directory
        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val fileName = "LovelyPos($dateStr)Report.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            PdfFileResult(uri, file, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generateSummaryPdf(
        context: Context,
        report: SummaryReport,
        dateRangeLabel: String
    ): Uri? {
        return generateSummaryPdfResult(context, report, dateRangeLabel)?.uri
    }

    private fun drawSummaryBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        bgPaint: Paint,
        borderPaint: Paint,
        titlePaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawRect(x, y, x + width, y + height, bgPaint)
        val paintBorder = Paint(borderPaint).apply { style = Paint.Style.STROKE }
        canvas.drawRect(x, y, x + width, y + height, paintBorder)

        canvas.drawText(title, x + 8f, y + 18f, titlePaint)
        val origValSize = valuePaint.textSize
        valuePaint.textSize = 11f
        canvas.drawText(value, x + 8f, y + 38f, valuePaint)
        valuePaint.textSize = origValSize
    }

    fun savePdfToDestination(context: Context, sourceFile: File, destinationUri: Uri): Boolean {
        return try {
            sourceFile.inputStream().use { input ->
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun sharePdf(context: Context, uri: Uri, fileName: String = "Laporan_Penjualan.pdf") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan $fileName"))
    }
}
