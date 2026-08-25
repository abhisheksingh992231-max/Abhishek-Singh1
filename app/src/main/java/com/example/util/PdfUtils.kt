package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.data.PdfActionType
import com.example.data.PdfDocumentEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.Locale

object PdfUtils {
    private const val TAG = "PdfUtils"

    data class CompressionResult(
        val originalSizeBytes: Long,
        val compressedSizeBytes: Long,
        val pageCount: Int,
        val compressionRatioPercent: Float,
        val outputFile: File
    )

    data class MergeResult(
        val totalPages: Int,
        val totalSizeBytes: Long,
        val outputFile: File
    )

    data class SplitResult(
        val extractedPageCount: Int,
        val totalSizeBytes: Long,
        val outputFile: File
    )

    /**
     * Format byte count into human-readable string (e.g. 1.2 MB, 450 KB).
     */
    fun formatFileSize(bytes: Long): String {
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var b = bytes
        while (b <= -999999 || b >= 999999) {
            b /= 1000
            ci.next()
        }
        return String.format(Locale.US, "%.1f %cB", b / 1000.0, ci.current())
    }

    /**
     * Create sample PDFs in context.filesDir / samples if they don't exist yet.
     */
    fun ensureSamplePdfsExist(context: Context): List<PdfDocumentEntity> {
        val samplesDir = File(context.filesDir, "samples")
        if (!samplesDir.exists()) {
            samplesDir.mkdirs()
        }

        val samples = mutableListOf<PdfDocumentEntity>()

        // Sample 1: Financial Report (3 pages)
        val file1 = File(samplesDir, "Sample_Financial_Report.pdf")
        if (!file1.exists()) {
            createSampleFinancialReport(file1)
        }
        if (file1.exists()) {
            samples.add(
                PdfDocumentEntity(
                    title = "Sample Financial Report",
                    filePath = file1.absolutePath,
                    originalSizeBytes = file1.length(),
                    compressedSizeBytes = file1.length(),
                    pageCount = 3,
                    actionType = PdfActionType.ORIGINAL,
                    compressionRatioPercent = 0f
                )
            )
        }

        // Sample 2: Project Proposal (2 pages)
        val file2 = File(samplesDir, "Project_Proposal_2026.pdf")
        if (!file2.exists()) {
            createSampleProjectProposal(file2)
        }
        if (file2.exists()) {
            samples.add(
                PdfDocumentEntity(
                    title = "Project Proposal 2026",
                    filePath = file2.absolutePath,
                    originalSizeBytes = file2.length(),
                    compressedSizeBytes = file2.length(),
                    pageCount = 2,
                    actionType = PdfActionType.ORIGINAL,
                    compressionRatioPercent = 0f
                )
            )
        }

        // Sample 3: Business Invoice (1 page)
        val file3 = File(samplesDir, "Business_Invoice_1042.pdf")
        if (!file3.exists()) {
            createSampleInvoice(file3)
        }
        if (file3.exists()) {
            samples.add(
                PdfDocumentEntity(
                    title = "Business Invoice #1042",
                    filePath = file3.absolutePath,
                    originalSizeBytes = file3.length(),
                    compressedSizeBytes = file3.length(),
                    pageCount = 1,
                    actionType = PdfActionType.ORIGINAL,
                    compressionRatioPercent = 0f
                )
            )
        }

        return samples
    }

    private fun createSampleFinancialReport(outputFile: File) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val paintTitle = Paint().apply {
            color = Color.rgb(26, 35, 126)
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(63, 81, 181)
            textSize = 14f
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            isAntiAlias = true
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(238, 238, 245)
        }

        // Page 1: Overview
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        canvas.drawRect(Rect(0, 0, pageWidth, 80), Paint().apply { color = Color.rgb(26, 35, 126) })
        paintTitle.color = Color.WHITE
        canvas.drawText("ANNUAL FINANCIAL REPORT 2026", 40f, 48f, paintTitle)

        paintTitle.color = Color.rgb(26, 35, 126)
        canvas.drawText("Executive Summary & Revenue Breakdown", 40f, 120f, paintSubtitle)
        canvas.drawText("Company: Global Solutions Inc.", 40f, 140f, paintBody)
        canvas.drawText("Period: Q1 - Q4 2025 Summary", 40f, 155f, paintBody)

        // Draw Table
        val tableTop = 190f
        canvas.drawRect(40f, tableTop, pageWidth - 40f, tableTop + 30f, paintHeaderBg)
        paintBody.isFakeBoldText = true
        canvas.drawText("Quarter", 50f, tableTop + 20f, paintBody)
        canvas.drawText("Revenue (\$M)", 180f, tableTop + 20f, paintBody)
        canvas.drawText("Expenses (\$M)", 320f, tableTop + 20f, paintBody)
        canvas.drawText("Net Profit (\$M)", 450f, tableTop + 20f, paintBody)

        paintBody.isFakeBoldText = false
        val rows = listOf(
            listOf("Q1 2025", "14.2", "9.8", "4.4"),
            listOf("Q2 2025", "16.8", "10.5", "6.3"),
            listOf("Q3 2025", "19.5", "11.2", "8.3"),
            listOf("Q4 2025", "22.1", "12.0", "10.1")
        )

        var yPos = tableTop + 50f
        for (row in rows) {
            canvas.drawText(row[0], 50f, yPos, paintBody)
            canvas.drawText(row[1], 180f, yPos, paintBody)
            canvas.drawText(row[2], 320f, yPos, paintBody)
            canvas.drawText(row[3], 450f, yPos, paintBody)
            canvas.drawLine(40f, yPos + 8f, pageWidth - 40f, yPos + 8f, Paint().apply { color = Color.LTGRAY })
            yPos += 30f
        }

        // Draw Chart graphics
        val chartTop = yPos + 40f
        canvas.drawText("Revenue Growth Chart", 40f, chartTop - 10f, paintSubtitle)

        val barColors = listOf(Color.rgb(63, 81, 181), Color.rgb(92, 107, 192), Color.rgb(121, 134, 203), Color.rgb(211, 47, 47))
        val barHeights = listOf(100f, 130f, 160f, 190f)

        for (i in 0..3) {
            val left = 70f + i * 110f
            val top = chartTop + 220f - barHeights[i]
            val right = left + 60f
            val bottom = chartTop + 220f
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, Paint().apply { color = barColors[i] })
            canvas.drawText(rows[i][0], left + 10f, bottom + 20f, paintBody)
        }

        document.finishPage(page)

        // Page 2: Analysis & Notes
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas

        canvas.drawText("Quarterly Profit & Loss Analysis", 40f, 60f, paintTitle)
        canvas.drawText("Strategic Highlights & Cost Optimization", 40f, 85f, paintSubtitle)

        val paragraphText = listOf(
            "During the fiscal year 2025, Global Solutions Inc. achieved a solid 28% year-over-year revenue growth.",
            "Key drivers included expansion into cloud automation software, enterprise security tools, and data analytics.",
            "Operating margins improved by 4.2 percentage points due to streamlined logistics and cloud infrastructure efficiency.",
            "R&D investments were increased by 15% to support next-generation generative AI workflows and PDF document security.",
            "Cash flow from operations totaled $29.1M, supporting planned strategic acquisitions for H1 2026."
        )

        var textY = 130f
        for (p in paragraphText) {
            canvas.drawCircle(50f, textY - 4f, 4f, Paint().apply { color = Color.rgb(211, 47, 47) })
            canvas.drawText(p, 65f, textY, paintBody)
            textY += 35f
        }

        // Decorative box
        canvas.drawRoundRect(
            RectF(40f, textY + 20f, pageWidth - 40f, textY + 160f),
            12f, 12f,
            Paint().apply { color = Color.rgb(240, 244, 255) }
        )
        canvas.drawText("Auditor Statement & Compliance Notes", 60f, textY + 50f, paintSubtitle)
        canvas.drawText("This financial summary has been audited in accordance with International Financial Reporting Standards (IFRS).", 60f, textY + 80f, paintBody)
        canvas.drawText("All financial metrics reflect consolidated balances as of December 31, 2025.", 60f, textY + 105f, paintBody)

        document.finishPage(page)

        // Page 3: Appendix
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas

        canvas.drawText("Appendix & Risk Disclosures", 40f, 60f, paintTitle)
        canvas.drawText("Forward-Looking Statements", 40f, 85f, paintSubtitle)

        canvas.drawText("This document contains forward-looking statements regarding operational expectations, net margins,", 40f, 130f, paintBody)
        canvas.drawText("and market expansion. Actual outcomes may differ based on market dynamics, exchange rates, and regulatory changes.", 40f, 150f, paintBody)

        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun createSampleProjectProposal(outputFile: File) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val paintTitle = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(211, 47, 47)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(66, 66, 66)
            textSize = 11f
            isAntiAlias = true
        }

        // Page 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Header Banner
        canvas.drawRect(Rect(0, 0, pageWidth, 120), Paint().apply { color = Color.rgb(211, 47, 47) })
        paintTitle.color = Color.WHITE
        canvas.drawText("MOBILE APPLICATION PROPOSAL", 40f, 65f, paintTitle)
        paintBody.color = Color.rgb(255, 235, 238)
        canvas.drawText("PDF Compressor & Document Suite Project", 40f, 92f, paintBody)

        paintTitle.color = Color.rgb(33, 33, 33)
        paintBody.color = Color.rgb(66, 66, 66)

        canvas.drawText("1. Executive Objective", 40f, 160f, paintSubtitle)
        canvas.drawText("To build an offline-first high-performance PDF compression and merging suite for Android,", 40f, 185f, paintBody)
        canvas.drawText("empowering users to reduce file sizes by up to 80% with zero quality compromise.", 40f, 205f, paintBody)

        canvas.drawText("2. Key Functional Modules", 40f, 245f, paintSubtitle)
        val modules = listOf(
            "• PDF Compression: Smart DPI resolution scaling & JPEG re-quantization",
            "• PDF Merging: Combine multi-source PDFs with page re-ordering",
            "• Page Extractor: Extract target page ranges (e.g. 1-3) into fresh PDFs",
            "• Image-to-PDF: Instant photo to document scanner & layout builder",
            "• Local Room Vault: Store history, track storage bytes saved reactively"
        )
        var y = 275f
        for (m in modules) {
            canvas.drawText(m, 50f, y, paintBody)
            y += 26f
        }

        document.finishPage(page)

        // Page 2
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas

        canvas.drawText("3. Delivery Timeline & Sign-off", 40f, 60f, paintSubtitle)
        canvas.drawText("Phase 1: Architecture & Room DB Engine - Done", 40f, 90f, paintBody)
        canvas.drawText("Phase 2: PDF Compression & Render Engine - Done", 40f, 115f, paintBody)
        canvas.drawText("Phase 3: Material 3 UI & Accessibility Polish - Done", 40f, 140f, paintBody)

        // Signature box
        canvas.drawRoundRect(RectF(40f, 200f, 260f, 290f), 8f, 8f, Paint().apply { color = Color.rgb(245, 245, 245) })
        canvas.drawText("Approved By: Product Manager", 50f, 230f, paintBody)
        canvas.drawLine(50f, 270f, 250f, 270f, Paint().apply { color = Color.GRAY })

        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun createSampleInvoice(outputFile: File) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val paintTitle = Paint().apply {
            color = Color.rgb(26, 35, 126)
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            isAntiAlias = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawText("INVOICE #1042", 40f, 65f, paintTitle)
        canvas.drawText("Date: August 25, 2026", 40f, 90f, paintBody)
        canvas.drawText("Due Date: September 15, 2026", 40f, 105f, paintBody)

        canvas.drawText("Billed To:", 350f, 65f, Paint(paintBody).apply { isFakeBoldText = true })
        canvas.drawText("Acme Corporation", 350f, 85f, paintBody)
        canvas.drawText("742 Evergreen Terrace", 350f, 100f, paintBody)

        // Table Header
        val top = 150f
        canvas.drawRect(40f, top, pageWidth - 40f, top + 30f, Paint().apply { color = Color.rgb(26, 35, 126) })
        val paintWhite = Paint(paintBody).apply { color = Color.WHITE; isFakeBoldText = true }
        canvas.drawText("Description", 50f, top + 20f, paintWhite)
        canvas.drawText("Hours", 320f, top + 20f, paintWhite)
        canvas.drawText("Rate", 400f, top + 20f, paintWhite)
        canvas.drawText("Total ($)", 480f, top + 20f, paintWhite)

        var y = top + 50f
        val lineItems = listOf(
            listOf("PDF Engine Optimization & Integration", "40", "$120", "$4,800.00"),
            listOf("Material 3 UI Theme & Adaptive Layouts", "25", "$120", "$3,000.00"),
            listOf("Unit & Room Persistence Setup", "15", "$120", "$1,800.00")
        )

        for (item in lineItems) {
            canvas.drawText(item[0], 50f, y, paintBody)
            canvas.drawText(item[1], 320f, y, paintBody)
            canvas.drawText(item[2], 400f, y, paintBody)
            canvas.drawText(item[3], 480f, y, paintBody)
            canvas.drawLine(40f, y + 8f, pageWidth - 40f, y + 8f, Paint().apply { color = Color.LTGRAY })
            y += 35f
        }

        // Total
        canvas.drawText("Subtotal:", 380f, y + 20f, Paint(paintBody).apply { isFakeBoldText = true })
        canvas.drawText("$9,600.00", 480f, y + 20f, paintBody)

        canvas.drawText("Tax (10%):", 380f, y + 40f, Paint(paintBody).apply { isFakeBoldText = true })
        canvas.drawText("$960.00", 480f, y + 40f, paintBody)

        canvas.drawText("Total Due:", 380f, y + 70f, Paint(paintBody).apply { isFakeBoldText = true; textSize = 14f; color = Color.rgb(211, 47, 47) })
        canvas.drawText("$10,560.00", 480f, y + 70f, Paint(paintBody).apply { isFakeBoldText = true; textSize = 14f; color = Color.rgb(211, 47, 47) })

        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    /**
     * Compress a PDF file by rendering pages onto scaled down Bitmaps with configurable JPEG quality.
     * @param scaleFactor Range 0.3f (extreme) to 1.0f (high quality)
     * @param jpegQuality Range 20 to 90
     */
    fun compressPdf(
        context: Context,
        inputPath: String,
        outputFileName: String,
        scaleFactor: Float = 0.6f,
        jpegQuality: Int = 50
    ): CompressionResult {
        val inputFile = File(inputPath)
        require(inputFile.exists()) { "Input PDF file does not exist: $inputPath" }

        val originalSize = inputFile.length()
        val processedDir = File(context.filesDir, "processed")
        if (!processedDir.exists()) processedDir.mkdirs()

        val outputFile = File(processedDir, outputFileName)

        val fileDescriptor = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        val pageCount = pdfRenderer.pageCount

        val pdfDocument = PdfDocument()

        try {
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)

                // Native page dimensions in points (1 pt = 1/72 inch)
                val originalWidth = page.width
                val originalHeight = page.height

                // Render dimensions based on scale factor
                val renderWidth = (originalWidth * scaleFactor * 2).toInt().coerceAtLeast(100)
                val renderHeight = (originalHeight * scaleFactor * 2).toInt().coerceAtLeast(100)

                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Compress bitmap with JPEG
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, bos)
                val compressedBytes = bos.toByteArray()
                bitmap.recycle()

                val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

                // Write compressed bitmap into new PdfDocument Page
                val pageInfo = PdfDocument.PageInfo.Builder(originalWidth, originalHeight, i + 1).create()
                val newPage = pdfDocument.startPage(pageInfo)
                val canvas = newPage.canvas

                canvas.drawBitmap(compressedBitmap, null, RectF(0f, 0f, originalWidth.toFloat(), originalHeight.toFloat()), null)
                pdfDocument.finishPage(newPage)

                compressedBitmap.recycle()
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
            pdfRenderer.close()
            fileDescriptor.close()
        }

        val compressedSize = outputFile.length()
        val savedBytes = (originalSize - compressedSize).coerceAtLeast(0)
        val ratio = if (originalSize > 0) (savedBytes.toFloat() / originalSize.toFloat()) * 100f else 0f

        return CompressionResult(
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressedSize,
            pageCount = pageCount,
            compressionRatioPercent = ratio,
            outputFile = outputFile
        )
    }

    /**
     * Merge multiple PDF files into one.
     */
    fun mergePdfs(
        context: Context,
        inputPaths: List<String>,
        outputFileName: String
    ): MergeResult {
        require(inputPaths.isNotEmpty()) { "Input PDF paths must not be empty" }

        val processedDir = File(context.filesDir, "processed")
        if (!processedDir.exists()) processedDir.mkdirs()

        val outputFile = File(processedDir, outputFileName)
        val pdfDocument = PdfDocument()
        var masterPageCounter = 0

        try {
            for (path in inputPaths) {
                val file = File(path)
                if (!file.exists()) continue

                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)

                for (p in 0 until renderer.pageCount) {
                    val page = renderer.openPage(p)
                    val w = page.width
                    val h = page.height

                    // Render at high clarity (2x scale)
                    val bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    masterPageCounter++
                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, masterPageCounter).create()
                    val newPage = pdfDocument.startPage(pageInfo)
                    val canvas = newPage.canvas

                    canvas.drawBitmap(bitmap, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), null)
                    pdfDocument.finishPage(newPage)

                    bitmap.recycle()
                }

                renderer.close()
                pfd.close()
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
        }

        return MergeResult(
            totalPages = masterPageCounter,
            totalSizeBytes = outputFile.length(),
            outputFile = outputFile
        )
    }

    /**
     * Split/Extract specific pages from a PDF.
     * selectedPageIndices is 0-indexed list of page numbers.
     */
    fun splitPdf(
        context: Context,
        inputPath: String,
        selectedPageIndices: List<Int>,
        outputFileName: String
    ): SplitResult {
        val inputFile = File(inputPath)
        require(inputFile.exists()) { "Input PDF file does not exist: $inputPath" }

        val processedDir = File(context.filesDir, "processed")
        if (!processedDir.exists()) processedDir.mkdirs()

        val outputFile = File(processedDir, outputFileName)
        val pdfDocument = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        var newPageIndex = 0
        try {
            for (index in selectedPageIndices) {
                if (index in 0 until renderer.pageCount) {
                    val page = renderer.openPage(index)
                    val w = page.width
                    val h = page.height

                    val bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    newPageIndex++
                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, newPageIndex).create()
                    val newPage = pdfDocument.startPage(pageInfo)
                    val canvas = newPage.canvas

                    canvas.drawBitmap(bitmap, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), null)
                    pdfDocument.finishPage(newPage)

                    bitmap.recycle()
                }
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
            renderer.close()
            pfd.close()
        }

        return SplitResult(
            extractedPageCount = newPageIndex,
            totalSizeBytes = outputFile.length(),
            outputFile = outputFile
        )
    }

    /**
     * Convert list of Bitmaps into a single PDF document.
     */
    fun imagesToPdf(
        context: Context,
        bitmaps: List<Bitmap>,
        outputFileName: String
    ): File {
        val processedDir = File(context.filesDir, "processed")
        if (!processedDir.exists()) processedDir.mkdirs()

        val outputFile = File(processedDir, outputFileName)
        val pdfDocument = PdfDocument()

        val standardWidth = 595
        val standardHeight = 842

        try {
            for ((index, bmp) in bitmaps.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(standardWidth, standardHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // White background
                canvas.drawColor(Color.WHITE)

                // Calculate fit scale preserving aspect ratio
                val scale = Math.min(
                    (standardWidth - 40f) / bmp.width.toFloat(),
                    (standardHeight - 40f) / bmp.height.toFloat()
                )
                val targetW = bmp.width * scale
                val targetH = bmp.height * scale
                val left = (standardWidth - targetW) / 2f
                val top = (standardHeight - targetH) / 2f

                canvas.drawBitmap(bmp, null, RectF(left, top, left + targetW, top + targetH), null)
                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
        }

        return outputFile
    }

    /**
     * Render thumbnail for a specific page of a PDF file.
     */
    fun renderPageThumbnail(
        context: Context,
        pdfPath: String,
        pageIndex: Int = 0,
        targetWidth: Int = 300
    ): Bitmap? {
        val file = File(pdfPath)
        if (!file.exists()) return null

        return try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            if (pageIndex !in 0 until renderer.pageCount) {
                renderer.close()
                pfd.close()
                return null
            }

            val page = renderer.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(100)

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            pfd.close()

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering thumbnail for $pdfPath", e)
            null
        }
    }

    /**
     * Render thumbnails for ALL pages of a PDF file.
     */
    fun renderAllPageThumbnails(
        context: Context,
        pdfPath: String,
        targetWidth: Int = 200
    ): List<Bitmap> {
        val file = File(pdfPath)
        if (!file.exists()) return emptyList()

        val thumbnails = mutableListOf<Bitmap>()
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val aspectRatio = page.height.toFloat() / page.width.toFloat()
                val targetHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(100)

                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                thumbnails.add(bitmap)
                page.close()
            }

            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering all page thumbnails for $pdfPath", e)
        }

        return thumbnails
    }

    /**
     * Inspect PDF page count safely.
     */
    fun getPdfPageCount(pdfPath: String): Int {
        val file = File(pdfPath)
        if (!file.exists()) return 0

        return try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        } catch (e: Exception) {
            0
        }
    }
}
