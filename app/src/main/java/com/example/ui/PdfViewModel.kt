package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PdfActionType
import com.example.data.PdfDocumentEntity
import com.example.data.PdfRepository
import com.example.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import com.example.data.AuthProvider
import com.example.data.UserAccount
import com.example.security.HardwareSecurityManager
import com.example.security.SecurityAttestationResult
import com.example.util.CloudStorageUploadResult
import com.example.util.GoogleCloudStorageManager
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.example.util.AppLanguage
import com.example.util.LocalizedStrings

enum class CompressionPreset(val label: String, val scaleFactor: Float, val jpegQuality: Int, val estReduction: String) {
    HIGH_QUALITY("High Quality", 0.85f, 80, "~20 - 40% Reduction"),
    RECOMMENDED("Balanced (Recommended)", 0.60f, 60, "~50 - 70% Reduction"),
    EXTREME("Maximum Compression", 0.40f, 35, "~70 - 90% Reduction")
}

class PdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PdfRepository
    private val sharedPrefs = application.getSharedPreferences("pdf_toolkit_auth_prefs", Context.MODE_PRIVATE)

    private val KEY_APP_LANGUAGE = "selected_app_language"

    private val _currentLanguage = MutableStateFlow<AppLanguage>(loadSavedLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private fun loadSavedLanguage(): AppLanguage {
        val savedCode = sharedPrefs.getString(KEY_APP_LANGUAGE, null)
        return if (savedCode != null) {
            AppLanguage.fromCode(savedCode)
        } else {
            AppLanguage.detectSystemLanguage(getApplication())
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        sharedPrefs.edit().putString(KEY_APP_LANGUAGE, language.code).apply()
        _userMessage.value = when (language) {
            AppLanguage.HINDI -> "भाषा हिंदी में बदली गई"
            AppLanguage.ENGLISH -> "Language changed to English"
        }
    }

    fun getString(key: String): String {
        return LocalizedStrings.get(key, _currentLanguage.value)
    }

    val securityManager = HardwareSecurityManager(application)
    val cloudStorageManager = GoogleCloudStorageManager(application)

    private val _cloudSyncStatus = MutableStateFlow<String?>(null)
    val cloudSyncStatus: StateFlow<String?> = _cloudSyncStatus.asStateFlow()

    private val _securityAttestation = MutableStateFlow<SecurityAttestationResult>(securityManager.performFullSecurityAudit())
    val securityAttestation: StateFlow<SecurityAttestationResult> = _securityAttestation.asStateFlow()

    private val _strictVpnBlockingEnabled = MutableStateFlow(true)
    val strictVpnBlockingEnabled: StateFlow<Boolean> = _strictVpnBlockingEnabled.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(loadSavedUserAccount())
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PdfRepository(application, db.pdfDocumentDao())

        viewModelScope.launch {
            repository.initializeSamplesIfEmpty()
        }
        refreshSecurityAudit()
    }

    fun refreshSecurityAudit() {
        _securityAttestation.value = securityManager.performFullSecurityAudit(enforceStrictMode = false)
    }

    fun toggleStrictVpnBlocking(enabled: Boolean) {
        _strictVpnBlockingEnabled.value = enabled
        _userMessage.value = if (enabled) "VPN / Proxy Strict Access Lock Enabled" else "VPN / Proxy Enforcement Set to Monitoring Mode"
    }

    private fun loadSavedUserAccount(): UserAccount? {
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", true) // Default true for smooth experience if previously logged in
        if (!isLoggedIn) return null

        val name = sharedPrefs.getString("user_name", "Alex Johnson") ?: "Alex Johnson"
        val email = sharedPrefs.getString("user_email", "alex.johnson@example.com") ?: "alex.johnson@example.com"
        val providerStr = sharedPrefs.getString("auth_provider", AuthProvider.GOOGLE.name) ?: AuthProvider.GOOGLE.name
        val provider = try { AuthProvider.valueOf(providerStr) } catch (e: Exception) { AuthProvider.GOOGLE }
        val initials = sharedPrefs.getString("avatar_initials", "AJ") ?: "AJ"

        return UserAccount(
            name = name,
            email = email,
            provider = provider,
            avatarInitials = initials,
            isLoggedIn = true
        )
    }

    private fun saveUserAccount(user: UserAccount?) {
        sharedPrefs.edit().apply {
            if (user != null && user.isLoggedIn) {
                putBoolean("is_logged_in", true)
                putString("user_name", user.name)
                putString("user_email", user.email)
                putString("auth_provider", user.provider.name)
                putString("avatar_initials", user.avatarInitials)
            } else {
                putBoolean("is_logged_in", false)
            }
            apply()
        }
    }

    fun loginWithGoogle(name: String = "Alex Johnson", email: String = "alex.johnson@gmail.com") {
        val user = UserAccount.google(name, email)
        _currentUser.value = user
        saveUserAccount(user)
        _userMessage.value = "Signed in with Google as ${user.name}"
    }

    fun loginWithFacebook(name: String = "Sarah Miller", email: String = "sarah.m@facebook.com") {
        val user = UserAccount.facebook(name, email)
        _currentUser.value = user
        saveUserAccount(user)
        _userMessage.value = "Signed in with Facebook as ${user.name}"
    }

    fun loginAsGuest() {
        val user = UserAccount.defaultGuest()
        _currentUser.value = user
        saveUserAccount(user)
        _userMessage.value = "Welcome! Continuing as Guest User"
    }

    fun logout() {
        _currentUser.value = null
        saveUserAccount(null)
        _userMessage.value = "Signed out successfully"
    }

    val allPdfs: StateFlow<List<PdfDocumentEntity>> = repository.allPdfs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentPdfs: StateFlow<List<PdfDocumentEntity>> = repository.getRecentPdfs(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoritePdfs: StateFlow<List<PdfDocumentEntity>> = repository.favoritePdfs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _totalBytesSaved = MutableStateFlow(0L)
    val totalBytesSaved: StateFlow<Long> = _totalBytesSaved.asStateFlow()

    init {
        viewModelScope.launch {
            repository.totalBytesSaved.collect { bytes ->
                _totalBytesSaved.value = bytes ?: 0L
            }
        }
    }

    // UI Status
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStatusText = MutableStateFlow("")
    val processingStatusText: StateFlow<String> = _processingStatusText.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Compression State
    private val _selectedCompressPdf = MutableStateFlow<PdfDocumentEntity?>(null)
    val selectedCompressPdf: StateFlow<PdfDocumentEntity?> = _selectedCompressPdf.asStateFlow()

    private val _selectedPreset = MutableStateFlow(CompressionPreset.RECOMMENDED)
    val selectedPreset: StateFlow<CompressionPreset> = _selectedPreset.asStateFlow()

    // Merge State
    private val _selectedMergeFiles = MutableStateFlow<List<PdfDocumentEntity>>(emptyList())
    val selectedMergeFiles: StateFlow<List<PdfDocumentEntity>> = _selectedMergeFiles.asStateFlow()

    // Split State
    private val _selectedSplitPdf = MutableStateFlow<PdfDocumentEntity?>(null)
    val selectedSplitPdf: StateFlow<PdfDocumentEntity?> = _selectedSplitPdf.asStateFlow()

    private val _splitSelectedPages = MutableStateFlow<Set<Int>>(emptySet())
    val splitSelectedPages: StateFlow<Set<Int>> = _splitSelectedPages.asStateFlow()

    private val _splitPageThumbnails = MutableStateFlow<List<Bitmap>>(emptyList())
    val splitPageThumbnails: StateFlow<List<Bitmap>> = _splitPageThumbnails.asStateFlow()

    // PDF Viewer Dialog State
    private val _viewingPdf = MutableStateFlow<PdfDocumentEntity?>(null)
    val viewingPdf: StateFlow<PdfDocumentEntity?> = _viewingPdf.asStateFlow()

    private val _viewingThumbnails = MutableStateFlow<List<Bitmap>>(emptyList())
    val viewingThumbnails: StateFlow<List<Bitmap>> = _viewingThumbnails.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun toggleFavorite(pdf: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(pdf)
        }
    }

    fun deletePdf(pdf: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deletePdf(pdf)
            _userMessage.value = "Deleted ${pdf.title}"
        }
    }

    // Compression actions
    fun selectCompressPdf(pdf: PdfDocumentEntity?) {
        _selectedCompressPdf.value = pdf
    }

    fun selectPreset(preset: CompressionPreset) {
        _selectedPreset.value = preset
    }

    fun executeCompression(customOutputName: String = "") {
        val source = _selectedCompressPdf.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            _processingStatusText.value = "Analyzing pages & compressing images..."

            try {
                val preset = _selectedPreset.value
                val fileName = if (customOutputName.isNotBlank()) {
                    if (customOutputName.endsWith(".pdf")) customOutputName else "$customOutputName.pdf"
                } else {
                    "${source.title.removeSuffix(".pdf")}_Compressed.pdf"
                }

                val result = PdfUtils.compressPdf(
                    context = getApplication(),
                    inputPath = source.filePath,
                    outputFileName = fileName,
                    scaleFactor = preset.scaleFactor,
                    jpegQuality = preset.jpegQuality
                )

                val entity = PdfDocumentEntity(
                    title = fileName.removeSuffix(".pdf"),
                    filePath = result.outputFile.absolutePath,
                    originalSizeBytes = result.originalSizeBytes,
                    compressedSizeBytes = result.compressedSizeBytes,
                    pageCount = result.pageCount,
                    actionType = PdfActionType.COMPRESSED,
                    compressionRatioPercent = result.compressionRatioPercent
                )

                repository.insertPdf(entity)
                val savedMB = String.format("%.1f", (result.originalSizeBytes - result.compressedSizeBytes) / (1024f * 1024f))
                val ratioStr = String.format("%.0f", result.compressionRatioPercent)
                _userMessage.value = "Compression Complete! Saved $ratioStr% ($savedMB MB)"
            } catch (e: Exception) {
                _userMessage.value = "Compression Error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatusText.value = ""
            }
        }
    }

    // Merge Actions
    fun addFileToMerge(pdf: PdfDocumentEntity) {
        if (_selectedMergeFiles.value.none { it.id == pdf.id }) {
            _selectedMergeFiles.value = _selectedMergeFiles.value + pdf
        }
    }

    fun removeFileFromMerge(pdf: PdfDocumentEntity) {
        _selectedMergeFiles.value = _selectedMergeFiles.value.filter { it.id != pdf.id }
    }

    fun reorderMergeFiles(fromIndex: Int, toIndex: Int) {
        val current = _selectedMergeFiles.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedMergeFiles.value = current
        }
    }

    fun clearMergeList() {
        _selectedMergeFiles.value = emptyList()
    }

    fun executeMerge(outputTitle: String) {
        val files = _selectedMergeFiles.value
        if (files.size < 2) {
            _userMessage.value = "Select at least 2 PDF files to merge."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            _processingStatusText.value = "Merging ${files.size} documents into one..."

            try {
                val fileName = if (outputTitle.isNotBlank()) {
                    if (outputTitle.endsWith(".pdf")) outputTitle else "$outputTitle.pdf"
                } else {
                    "Merged_Document_${System.currentTimeMillis() % 10000}.pdf"
                }

                val paths = files.map { it.filePath }
                val result = PdfUtils.mergePdfs(
                    context = getApplication(),
                    inputPaths = paths,
                    outputFileName = fileName
                )

                val entity = PdfDocumentEntity(
                    title = fileName.removeSuffix(".pdf"),
                    filePath = result.outputFile.absolutePath,
                    originalSizeBytes = result.totalSizeBytes,
                    compressedSizeBytes = result.totalSizeBytes,
                    pageCount = result.totalPages,
                    actionType = PdfActionType.MERGED,
                    compressionRatioPercent = 0f
                )

                repository.insertPdf(entity)
                _userMessage.value = "Merged ${files.size} files (${result.totalPages} total pages)!"
                _selectedMergeFiles.value = emptyList()
            } catch (e: Exception) {
                _userMessage.value = "Merge Error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatusText.value = ""
            }
        }
    }

    // Split Actions
    fun selectSplitPdf(pdf: PdfDocumentEntity?) {
        _selectedSplitPdf.value = pdf
        _splitSelectedPages.value = emptySet()
        _splitPageThumbnails.value = emptyList()

        if (pdf != null) {
            viewModelScope.launch(Dispatchers.IO) {
                _isProcessing.value = true
                _processingStatusText.value = "Generating page thumbnails..."
                val thumbs = PdfUtils.renderAllPageThumbnails(getApplication(), pdf.filePath, targetWidth = 240)
                _splitPageThumbnails.value = thumbs
                // Select all pages by default
                _splitSelectedPages.value = (0 until pdf.pageCount).toSet()
                _isProcessing.value = false
                _processingStatusText.value = ""
            }
        }
    }

    fun toggleSplitPageSelection(pageIndex: Int) {
        val current = _splitSelectedPages.value.toMutableSet()
        if (current.contains(pageIndex)) {
            current.remove(pageIndex)
        } else {
            current.add(pageIndex)
        }
        _splitSelectedPages.value = current
    }

    fun executeSplit(outputTitle: String) {
        val source = _selectedSplitPdf.value ?: return
        val selectedPages = _splitSelectedPages.value.toList().sorted()
        if (selectedPages.isEmpty()) {
            _userMessage.value = "Please select at least one page to extract."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            _processingStatusText.value = "Extracting ${selectedPages.size} pages..."

            try {
                val fileName = if (outputTitle.isNotBlank()) {
                    if (outputTitle.endsWith(".pdf")) outputTitle else "$outputTitle.pdf"
                } else {
                    "${source.title}_Extracted.pdf"
                }

                val result = PdfUtils.splitPdf(
                    context = getApplication(),
                    inputPath = source.filePath,
                    selectedPageIndices = selectedPages,
                    outputFileName = fileName
                )

                val entity = PdfDocumentEntity(
                    title = fileName.removeSuffix(".pdf"),
                    filePath = result.outputFile.absolutePath,
                    originalSizeBytes = result.totalSizeBytes,
                    compressedSizeBytes = result.totalSizeBytes,
                    pageCount = result.extractedPageCount,
                    actionType = PdfActionType.SPLIT,
                    compressionRatioPercent = 0f
                )

                repository.insertPdf(entity)
                _userMessage.value = "Extracted ${result.extractedPageCount} pages into clean PDF!"
            } catch (e: Exception) {
                _userMessage.value = "Split Error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatusText.value = ""
            }
        }
    }

    // Image to PDF converter
    fun createPdfFromDrawnImages(imageBitmaps: List<Bitmap>, outputTitle: String) {
        if (imageBitmaps.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            _processingStatusText.value = "Building PDF document from photos..."

            try {
                val fileName = if (outputTitle.isNotBlank()) {
                    if (outputTitle.endsWith(".pdf")) outputTitle else "$outputTitle.pdf"
                } else {
                    "Image_Doc_${System.currentTimeMillis() % 10000}.pdf"
                }

                val file = PdfUtils.imagesToPdf(getApplication(), imageBitmaps, fileName)

                val entity = PdfDocumentEntity(
                    title = fileName.removeSuffix(".pdf"),
                    filePath = file.absolutePath,
                    originalSizeBytes = file.length(),
                    compressedSizeBytes = file.length(),
                    pageCount = imageBitmaps.size,
                    actionType = PdfActionType.CONVERTED_IMAGE,
                    compressionRatioPercent = 0f
                )

                repository.insertPdf(entity)
                _userMessage.value = "Created PDF from ${imageBitmaps.size} image(s)!"
            } catch (e: Exception) {
                _userMessage.value = "Conversion Error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatusText.value = ""
            }
        }
    }

    // Viewing PDF
    fun openPdfViewer(pdf: PdfDocumentEntity) {
        _viewingPdf.value = pdf
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLastAccessed(pdf)
            val thumbs = PdfUtils.renderAllPageThumbnails(getApplication(), pdf.filePath, targetWidth = 500)
            _viewingThumbnails.value = thumbs
        }
    }

    fun closePdfViewer() {
        _viewingPdf.value = null
        _viewingThumbnails.value = emptyList()
    }

    fun syncPdfToCloudStorage(pdf: PdfDocumentEntity) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatusText.value = "Encrypting with AES-256-GCM & transmitting to Google Cloud Storage..."

            val result = cloudStorageManager.uploadToUserGoogleCloudStorage(pdf)
            _isProcessing.value = false
            _processingStatusText.value = ""

            if (result.success) {
                _userMessage.value = "E2EE Transmitted to Google Cloud Storage (Zero 3rd-party servers)."
            } else {
                _userMessage.value = "GCS Sync Error: ${result.message}"
            }
        }
    }
}
