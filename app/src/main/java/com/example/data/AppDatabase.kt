package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromActionType(value: PdfActionType): String = value.name

    @TypeConverter
    fun toActionType(value: String): PdfActionType = try {
        PdfActionType.valueOf(value)
    } catch (e: Exception) {
        PdfActionType.ORIGINAL
    }
}

@Database(entities = [PdfDocumentEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pdfDocumentDao(): PdfDocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_toolkit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
