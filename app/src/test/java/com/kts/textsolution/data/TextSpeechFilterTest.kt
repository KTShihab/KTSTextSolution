package com.kts.textsolution.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSpeechFilterTest {

    @Test
    fun testDegreeSymbolRemovalInNoneMode() {
        val input = "° Today's Top News °"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.None)
        assertEquals("Today's Top News", output)
    }

    @Test
    fun testDegreeSymbolWithTemperatureAndNumbersInNoneMode() {
        val input = "Temperature recorded at 32° today. Expect 34°C tomorrow."
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.None)
        assertEquals("Temperature recorded at 32 today. Expect 34 C tomorrow.", output)
    }

    @Test
    fun testNumbersAndPricingRetainedInNoneMode() {
        val input = "° Onion 1 kg for 35 rupees, tomato 40 rupees. Offer valid until 2026-09-15. Call: 9876543210"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.None)
        assertTrue(output.contains("35 rupees"))
        assertTrue(output.contains("40 rupees"))
        assertTrue(output.contains("2026-09-15"))
        assertFalse(output.contains("°"))
        assertFalse(output.contains("9876543210"))
    }

    @Test
    fun testBulletsAndDecorativeSymbolsInNoneMode() {
        val input = "• First · Second ★ Third ✔ Fourth ~Fifth~ *Sixth*"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.None)
        assertEquals("First Second Third Fourth Fifth Sixth", output)
    }

    @Test
    fun testDividersAndBorders() {
        val input = "=============================\nToday's News\n-----------------------------"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.None)
        assertEquals("Today's News", output)
    }

    @Test
    fun testAllModeLeavesTextUnchanged() {
        val input = "° News 😃 https://example.com 9876543210"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.All)
        assertEquals(input, output)
    }

    @Test
    fun testSomeModeRetainsUrlsAndPhoneNumbers() {
        val input = "° News https://example.com Call: 9876543210 😃"
        val output = TextSpeechFilter.filter(input, SpeechFilterMode.Some)
        assertFalse(output.contains("°"))
        assertFalse(output.contains("😃"))
        assertTrue(output.contains("https://example.com"))
        assertTrue(output.contains("9876543210"))
    }
}