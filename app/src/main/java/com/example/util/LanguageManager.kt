package com.example.util

import android.content.Context
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
    ENGLISH("en", "English", "English", "🇺🇸"),
    HINDI("hi", "Hindi", "हिंदी", "🇮🇳");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }

        fun detectSystemLanguage(context: Context): AppLanguage {
            val systemLocale = Locale.getDefault().language
            return if (systemLocale.equals("hi", ignoreCase = true)) HINDI else ENGLISH
        }
    }
}

object LocalizedStrings {

    fun get(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> hindiStrings[key] ?: englishStrings[key] ?: key
            AppLanguage.ENGLISH -> englishStrings[key] ?: key
        }
    }

    private val englishStrings = mapOf(
        "app_title" to "PDF ToolKit",
        "app_subtitle" to "Professional document utility",
        "welcome" to "Welcome",
        "guest_user" to "Guest User",
        "login_title" to "PDF Tools",
        "login_subtitle" to "Sign in to access document compression, merging, and secure vault storage",
        "continue_google" to "Continue with Google",
        "continue_facebook" to "Continue with Facebook",
        "continue_guest" to "Continue as Guest",
        "privacy_note" to "Your document vault is stored locally & encrypted",
        "nav_dashboard" to "Dashboard",
        "nav_compress" to "Compress",
        "nav_merge" to "Merge",
        "nav_split" to "Split",
        "nav_img2pdf" to "Img to PDF",
        "nav_vault" to "Vault",
        "action_compress" to "Compress PDF",
        "action_merge" to "Merge PDFs",
        "action_split" to "Split PDF",
        "action_img2pdf" to "Image to PDF",
        "total_documents" to "Total Documents",
        "saved_space" to "Storage Saved",
        "recent_documents" to "Recent Documents",
        "view_all" to "View All",
        "no_documents" to "No PDF documents found.",
        "security_defense_title" to "4-Layer Defense Protocol",
        "security_defense_subtitle" to "TEE Hardware Key Bound • No VPN Tunnels",
        "security_verified" to "VERIFIED",
        "security_alert" to "ALERT",
        "gcs_e2ee_title" to "Google Cloud Storage • E2EE Direct",
        "gcs_e2ee_subtitle" to "Bypassing 3rd-party servers • AES-256-GCM hardware key encryption",
        "gcs_sync_action" to "Cloud Sync (E2EE)",
        "language_selection" to "Language Selection",
        "select_language" to "Choose Preferred Language",
        "system_detected" to "System Detected",
        "sign_out" to "Sign Out",
        "close" to "Close"
    )

    private val hindiStrings = mapOf(
        "app_title" to "पीडीएफ टूलकिट",
        "app_subtitle" to "प्रोफेशनल दस्तावेज़ उपयोगिता",
        "welcome" to "स्वागत है",
        "guest_user" to "अतिथि उपयोगकर्ता",
        "login_title" to "पीडीएफ टूल",
        "login_subtitle" to "दस्तावेज़ संपीड़न, विलय और सुरक्षित वॉल्ट स्टोर का उपयोग करने के लिए साइन इन करें",
        "continue_google" to "गूगल के साथ जारी रखें",
        "continue_facebook" to "फेसबुक के साथ जारी रखें",
        "continue_guest" to "अतिथि के रूप में जारी रखें",
        "privacy_note" to "आपका दस्तावेज़ वॉल्ट स्थानीय रूप से और एन्क्रिप्टेड संग्रहीत है",
        "nav_dashboard" to "डैशबोर्ड",
        "nav_compress" to "कंप्रेस",
        "nav_merge" to "मर्ज",
        "nav_split" to "स्प्लिट",
        "nav_img2pdf" to "इमेज टू पीडीएफ",
        "nav_vault" to "वॉल्ट",
        "action_compress" to "पीडीएफ कंप्रेस करें",
        "action_merge" to "पीडीएफ विलय करें",
        "action_split" to "पीडीएफ विभाजित करें",
        "action_img2pdf" to "इमेज से पीडीएफ",
        "total_documents" to "कुल दस्तावेज़",
        "saved_space" to "सहेजी गई जगह",
        "recent_documents" to "हाल के दस्तावेज़",
        "view_all" to "सभी देखें",
        "no_documents" to "कोई पीडीएफ दस्तावेज़ नहीं मिला।",
        "security_defense_title" to "4-स्तरीय सुरक्षा प्रोटोकॉल",
        "security_defense_subtitle" to "TEE हार्डवेयर कुंजी से बंधा हुआ • वीपीएन सुरंग नहीं",
        "security_verified" to "सत्यापित",
        "security_alert" to "सचेत",
        "gcs_e2ee_title" to "गूगल क्लाउड स्टोरेज • सीधी ई2ईई",
        "gcs_e2ee_subtitle" to "तृतीय-पक्ष सर्वरों को बायपास करना • AES-256-GCM हार्डवेयर एन्क्रिप्शन",
        "gcs_sync_action" to "क्लाउड सिंक (E2EE)",
        "language_selection" to "भाषा चयन",
        "select_language" to "पसंदीदा भाषा चुनें",
        "system_detected" to "सिस्टम द्वारा पहचाना गया",
        "sign_out" to "साइन आउट करें",
        "close" to "बंद करें"
    )
}
