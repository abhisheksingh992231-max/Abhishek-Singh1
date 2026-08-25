package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDocumentDao {
    @Query("SELECT * FROM pdf_documents ORDER BY timestamp DESC")
    fun getAllPdfs(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPdfs(limit: Int): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id")
    suspend fun getPdfById(id: Long): PdfDocumentEntity?

    @Query("SELECT * FROM pdf_documents WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePdfs(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPdfs(query: String): Flow<List<PdfDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocumentEntity): Long

    @Update
    suspend fun updatePdf(pdf: PdfDocumentEntity)

    @Delete
    suspend fun deletePdf(pdf: PdfDocumentEntity)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(originalSizeBytes - compressedSizeBytes) FROM pdf_documents WHERE compressedSizeBytes > 0 AND compressedSizeBytes < originalSizeBytes")
    fun getTotalBytesSaved(): Flow<Long?>
}
