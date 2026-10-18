package com.kts.textsolution.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectorTest {

    @Test
    fun testHindiDetection() {
        val text = "नमस्ते, आप कैसे हैं?"
        assertEquals("hi", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testTamilDetection() {
        val text = "வணக்கம், நீங்கள் எப்படி இருக்கிறீர்கள்?"
        assertEquals("ta", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testTeluguDetection() {
        val text = "నమస్కారం, ఎలా ఉన్నారు?"
        assertEquals("te", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testKannadaDetection() {
        val text = "ನಮಸ್ಕಾರ, ಹೇಗಿದ್ದೀರಾ?"
        assertEquals("kn", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testBengaliDetection() {
        val text = "নমস্কার, কেমন আছেন?"
        assertEquals("bn", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testArabicDetection() {
        val text = "السلام عليكم ورحمة الله وبركاته"
        assertEquals("ar", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testUrduDetection() {
        val text = "آپ کیسے ہیں؟ سب ٹھیک ہے"
        assertEquals("ur", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testRussianDetection() {
        val text = "Привет, как твои дела?"
        assertEquals("ru", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testEnglishDetection() {
        val text = "Welcome to KTS Text Solution application"
        assertEquals("en", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testGermanDetection() {
        val text = "Guten Morgen, wie geht es Ihnen heute?"
        assertEquals("de", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testFrenchDetection() {
        val text = "Bonjour tout le monde, comment allez-vous?"
        assertEquals("fr", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testSpanishDetection() {
        val text = "Hola, ¿cómo estás hoy amigo?"
        assertEquals("es", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testJapaneseDetection() {
        val text = "こんにちは、元気ですか？"
        assertEquals("ja", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testChineseDetection() {
        val text = "你好，祝你有美好的一天"
        assertEquals("zh", LanguageDetector.detectLocale(text).language)
    }

    @Test
    fun testKoreanDetection() {
        val text = "안녕하세요, 반갑습니다"
        assertEquals("ko", LanguageDetector.detectLocale(text).language)
    }
}
