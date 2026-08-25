package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PdfActionType {
    COMPRESSED,
    MERGED,
    SPLIT,
    CONVERTED_IMAGE,
    ORIGINAL
}

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val pageCount: Int,
    val actionType: PdfActionType,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val compressionRatioPercent: Float = 0f
)
