package com.kts.textsolution.data

import java.util.Locale

object LanguageDetector {

    val HINDI_LOCALE = Locale("hi", "IN")
    val TAMIL_LOCALE = Locale("ta", "IN")
    val TELUGU_LOCALE = Locale("te", "IN")
    val KANNADA_LOCALE = Locale("kn", "IN")
    val BENGALI_LOCALE = Locale("bn", "IN")
    val GUJARATI_LOCALE = Locale("gu", "IN")
    val PUNJABI_LOCALE = Locale("pa", "IN")
    val ODIA_LOCALE = Locale("or", "IN")
    val ARABIC_LOCALE = Locale("ar")
    val URDU_LOCALE = Locale("ur", "IN")
    val PERSIAN_LOCALE = Locale("fa", "IR")
    val CYRILLIC_LOCALE = Locale("ru", "RU")
    val THAI_LOCALE = Locale("th", "TH")
    val HEBREW_LOCALE = Locale("he", "IL")
    val GREEK_LOCALE = Locale("el", "GR")
    val SINHALA_LOCALE = Locale("si", "LK")
    val MYANMAR_LOCALE = Locale("my", "MM")
    val KHMER_LOCALE = Locale("km", "KH")
    val LAO_LOCALE = Locale("lo", "LA")
    val ARMENIAN_LOCALE = Locale("hy", "AM")
    val GEORGIAN_LOCALE = Locale("ka", "GE")
    val SPANISH_LOCALE = Locale("es", "ES")
    val FRENCH_LOCALE = Locale.FRENCH
    val GERMAN_LOCALE = Locale.GERMAN
    val ITALIAN_LOCALE = Locale.ITALIAN
    val PORTUGUESE_LOCALE = Locale("pt", "BR")
    val TURKISH_LOCALE = Locale("tr", "TR")
    val VIETNAMESE_LOCALE = Locale("vi", "VN")
    val INDONESIAN_LOCALE = Locale("id", "ID")

    /**
     * Detects the best matching Locale for any given text.
     * Evaluates characters by Unicode script blocks and Latin language characteristics.
     */
    fun detectLocale(text: String): Locale {
        if (text.isBlank()) return Locale.getDefault()

        var devanagariCount = 0
        var tamilCount = 0
        var teluguCount = 0
        var kannadaCount = 0
        var bengaliCount = 0
        var gujaratiCount = 0
        var gurmukhiCount = 0
        var odiaCount = 0
        var arabicCount = 0
        var urduSpecificCount = 0
        var persianSpecificCount = 0
        var cyrillicCount = 0
        var greekCount = 0
        var hebrewCount = 0
        var thaiCount = 0
        var sinhalaCount = 0
        var myanmarCount = 0
        var khmerCount = 0
        var laoCount = 0
        var japaneseKanaCount = 0
        var koreanCount = 0
        var cjkCount = 0
        var armenianCount = 0
        var georgianCount = 0
        var latinCount = 0

        for (ch in text) {
            when (ch) {
                in '\u0900'..'\u097F' -> devanagariCount++
                in '\u0B80'..'\u0BFF' -> tamilCount++
                in '\u0C00'..'\u0C7F' -> teluguCount++
                in '\u0C80'..'\u0CFF' -> kannadaCount++
                in '\u0980'..'\u09FF' -> bengaliCount++
                in '\u0A80'..'\u0AFF' -> gujaratiCount++
                in '\u0A00'..'\u0A7F' -> gurmukhiCount++
                in '\u0B00'..'\u0B7F' -> odiaCount++
                in '\u0600'..'\u06FF', in '\u0750'..'\u077F', in '\u08A0'..'\u08FF' -> {
                    arabicCount++
                    // Check for Urdu specific characters: ٹ, ڈ, ڑ, ں, ے, ہ, ھ
                    if (ch in listOf('\u0679', '\u0688', '\u0691', '\u06BA', '\u06D2', '\u06C1', '\u06BE')) {
                        urduSpecificCount++
                    } else if (ch in listOf('\u067E', '\u0686', '\u0698', '\u06AF')) {
                        persianSpecificCount++
                    }
                }
                in '\u0400'..'\u04FF', in '\u0500'..'\u052F' -> cyrillicCount++
                in '\u0370'..'\u03FF', in '\u1F00'..'\u1FFF' -> greekCount++
                in '\u0590'..'\u05FF' -> hebrewCount++
                in '\u0E00'..'\u0E7F' -> thaiCount++
                in '\u0D80'..'\u0DFF' -> sinhalaCount++
                in '\u1000'..'\u109F' -> myanmarCount++
                in '\u1780'..'\u17FF' -> khmerCount++
                in '\u0E80'..'\u0EFF' -> laoCount++
                in '\u3040'..'\u309F', in '\u30A0'..'\u30FF' -> japaneseKanaCount++
                in '\uAC00'..'\uD7AF', in '\u1100'..'\u11FF', in '\u3130'..'\u318F' -> koreanCount++
                in '\u4E00'..'\u9FFF', in '\u3400'..'\u4DBF' -> cjkCount++
                in '\u0530'..'\u058F' -> armenianCount++
                in '\u10A0'..'\u10FF' -> georgianCount++
                in 'a'..'z', in 'A'..'Z', in '\u00C0'..'\u024F' -> latinCount++
            }
        }

        // Check non-Latin scripts first
        val scriptScores = listOf(
            devanagariCount to HINDI_LOCALE,
            tamilCount to TAMIL_LOCALE,
            teluguCount to TELUGU_LOCALE,
            kannadaCount to KANNADA_LOCALE,
            bengaliCount to BENGALI_LOCALE,
            gujaratiCount to GUJARATI_LOCALE,
            gurmukhiCount to PUNJABI_LOCALE,
            odiaCount to ODIA_LOCALE,
            (if (urduSpecificCount > 0) arabicCount else 0) to URDU_LOCALE,
            (if (persianSpecificCount > 0 && urduSpecificCount == 0) arabicCount else 0) to PERSIAN_LOCALE,
            (if (urduSpecificCount == 0 && persianSpecificCount == 0) arabicCount else 0) to ARABIC_LOCALE,
            cyrillicCount to CYRILLIC_LOCALE,
            greekCount to GREEK_LOCALE,
            hebrewCount to HEBREW_LOCALE,
            thaiCount to THAI_LOCALE,
            sinhalaCount to SINHALA_LOCALE,
            myanmarCount to MYANMAR_LOCALE,
            khmerCount to KHMER_LOCALE,
            laoCount to LAO_LOCALE,
            japaneseKanaCount to Locale.JAPANESE,
            koreanCount to Locale.KOREAN,
            cjkCount to Locale.SIMPLIFIED_CHINESE,
            armenianCount to ARMENIAN_LOCALE,
            georgianCount to GEORGIAN_LOCALE,
        )

        val bestNonLatin = scriptScores.maxByOrNull { it.first }
        if (bestNonLatin != null && bestNonLatin.first > 0) {
            // If Japanese Kana is present, prefer Japanese even if Kanji (CJK Ideographs) exist
            if (japaneseKanaCount > 0) {
                return Locale.JAPANESE
            }
            return bestNonLatin.second
        }

        // Latin script language detection
        if (latinCount > 0) {
            return detectLatinLanguage(text)
        }

        return Locale.getDefault()
    }

    private fun detectLatinLanguage(text: String): Locale {
        val lower = text.lowercase(Locale.ROOT)

        // Vietnamese specific diacritics
        if (lower.any { it in "đơưảẵẳằặấầẩẫậếềểễệốồổỗộớờởỡợứừửữự" }) {
            return VIETNAMESE_LOCALE
        }
        // Turkish
        if (lower.any { it in "ığş" }) {
            return TURKISH_LOCALE
        }

        val words = lower.split(Regex("[^\\p{L}]+")).filter { it.isNotBlank() }.toSet()

        // German words & umlauts
        val germanWords = setOf("der", "die", "das", "und", "ist", "nicht", "ein", "eine", "einer", "mit", "für", "auf", "von", "zu", "sich", "dem", "den", "hallo", "guten", "morgen", "tag", "abend")
        val hasGermanChars = lower.any { it in "äöüß" }
        if (hasGermanChars || words.count { it in germanWords } >= 2) {
            return GERMAN_LOCALE
        }

        // French words & accents
        val frenchWords = setOf("le", "la", "les", "des", "un", "une", "et", "est", "dans", "pour", "avec", "sur", "qui", "que", "ce", "cette", "bonjour", "merci", "monde")
        val hasFrenchChars = lower.any { it in "éèêëàâùûçœ" }
        if ((hasFrenchChars && words.count { it in frenchWords } >= 1) || words.count { it in frenchWords } >= 2) {
            return FRENCH_LOCALE
        }

        // Spanish words & characters
        val spanishWords = setOf("el", "la", "los", "las", "un", "una", "unos", "unas", "en", "por", "para", "con", "del", "al", "es", "son", "como", "hola", "buenos", "dias", "gracias")
        val hasSpanishChars = lower.any { it in "ñ¿¡" }
        if (hasSpanishChars || words.count { it in spanishWords } >= 2) {
            return SPANISH_LOCALE
        }

        // Portuguese words & characters
        val portugueseWords = setOf("não", "para", "com", "os", "as", "um", "uma", "em", "do", "da", "dos", "das", "no", "na", "nos", "nas", "pelo", "pela", "olá", "obrigado", "bom", "dia")
        val hasPortugueseChars = lower.any { it in "ãõ" }
        if (hasPortugueseChars || words.count { it in portugueseWords } >= 2) {
            return PORTUGUESE_LOCALE
        }

        // Italian words
        val italianWords = setOf("il", "lo", "la", "gli", "le", "del", "della", "dei", "degli", "delle", "con", "per", "non", "sono", "questo", "questa", "ciao", "grazie")
        if (words.count { it in italianWords } >= 2) {
            return ITALIAN_LOCALE
        }

        // Indonesian / Malay
        val indonesianWords = setOf("yang", "dan", "di", "ini", "itu", "untuk", "dari", "dalam", "tidak", "dengan", "ke", "pada", "adalah")
        if (words.count { it in indonesianWords } >= 2) {
            return INDONESIAN_LOCALE
        }

        return Locale.ENGLISH
    }
}
