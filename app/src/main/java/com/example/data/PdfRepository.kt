package com.example.data

import android.content.Context
import com.example.util.PdfUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class PdfRepository(
    private val context: Context,
    private val dao: PdfDocumentDao
) {
    val allPdfs: Flow<List<PdfDocumentEntity>> = dao.getAllPdfs()
    val favoritePdfs: Flow<List<PdfDocumentEntity>> = dao.getFavoritePdfs()
    val totalBytesSaved: Flow<Long?> = dao.getTotalBytesSaved()

    fun getRecentPdfs(limit: Int = 5): Flow<List<PdfDocumentEntity>> = dao.getRecentPdfs(limit)

    suspend fun updateLastAccessed(pdf: PdfDocumentEntity) {
        dao.updatePdf(pdf.copy(timestamp = System.currentTimeMillis()))
    }

    suspend fun initializeSamplesIfEmpty() {
        val currentPdfs = dao.getAllPdfs().first()
        if (currentPdfs.isEmpty()) {
            val samples = PdfUtils.ensureSamplePdfsExist(context)
            for (sample in samples) {
                dao.insertPdf(sample)
            }
        }
    }

    suspend fun insertPdf(pdf: PdfDocumentEntity): Long = dao.insertPdf(pdf)

    suspend fun updatePdf(pdf: PdfDocumentEntity) = dao.updatePdf(pdf)

    suspend fun deletePdf(pdf: PdfDocumentEntity) {
        val file = File(pdf.filePath)
        if (file.exists()) {
            file.delete()
        }
        dao.deletePdf(pdf)
    }

    suspend fun toggleFavorite(pdf: PdfDocumentEntity) {
        dao.updatePdf(pdf.copy(isFavorite = !pdf.isFavorite))
    }

    fun searchPdfs(query: String): Flow<List<PdfDocumentEntity>> = dao.searchPdfs(query)
}
