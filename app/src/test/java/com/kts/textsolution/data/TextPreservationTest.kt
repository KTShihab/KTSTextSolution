package com.kts.textsolution.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextPreservationTest {

    @Test
    fun testWordSplitPreservesPunctuationAndSymbols() {
        val input = "Hello, how are you? Doing great! #poetry @friend - 100% super! 😃"
        val chunks = TextSplitter.split(input, SplitMode.Word)
        val joined = TextSplitter.join(chunks, SplitMode.Word)

        // Verify punctuation marks and symbols are not stripped
        assertTrue(joined.contains(","))
        assertTrue(joined.contains("?"))
        assertTrue(joined.contains("!"))
        assertTrue(joined.contains("#poetry"))
        assertTrue(joined.contains("@friend"))
        assertTrue(joined.contains("100%"))
        assertTrue(joined.contains("😃"))
    }

    @Test
    fun testParagraphSplitPreservesContent() {
        val input = "Paragraph one: main information.\n\nParagraph two: more details."
        val chunks = TextSplitter.split(input, SplitMode.Paragraph)
        assertEquals(2, chunks.size)
        assertEquals("Paragraph one: main information.", chunks[0].text)
        assertEquals("Paragraph two: more details.", chunks[1].text)
    }

    @Test
    fun testLineSplitPreservesLinesAndJoin() {
        val input = "Line one\nLine two\r\nLine three"
        val chunks = TextSplitter.split(input, SplitMode.Line)
        assertEquals(3, chunks.size)
        assertEquals("Line one", chunks[0].text)
        assertEquals("Line two", chunks[1].text)
        assertEquals("Line three", chunks[2].text)
        val joined = TextSplitter.join(chunks, SplitMode.Line)
        assertEquals("Line one\nLine two\nLine three", joined)
    }

    @Test
    fun testUrlExtractorDoesNotMistakeNormalTextForUrl() {
        assertFalse(WebContentExtractor.isUrl("section.today"))
        assertFalse(WebContentExtractor.isUrl("This is a file.txt document."))
        assertFalse(WebContentExtractor.isUrl("sample.info text"))
        assertTrue(WebContentExtractor.isUrl("https://example.com"))
        assertTrue(WebContentExtractor.isUrl("http://test.in/page"))
        assertTrue(WebContentExtractor.isUrl("www.google.com"))
    }

    @Test
    fun testSentenceSplitHandlesAbbreviationsAndInitials() {
        val input1 = "U. S. A. is a large country."
        val chunks1 = TextSplitter.split(input1, SplitMode.Sentence)
        assertEquals(1, chunks1.size)
        assertEquals("U. S. A. is a large country.", chunks1[0].text)

        val input2 = "Which direction should we take from Govt. H. S. S. to reach the dining hall?"
        val chunks2 = TextSplitter.split(input2, SplitMode.Sentence)
        assertEquals(1, chunks2.size)
        assertEquals(input2, chunks2[0].text)

        val input3 = "Dr. B. R. Ambedkar was the architect of the Constitution. He was a great leader."
        val chunks3 = TextSplitter.split(input3, SplitMode.Sentence)
        assertEquals(2, chunks3.size)
        assertEquals("Dr. B. R. Ambedkar was the architect of the Constitution.", chunks3[0].text)
        assertEquals("He was a great leader.", chunks3[1].text)
    }

    @Test
    fun testSentenceSplitHandlesEnglishAbbreviationsAndNumberLists() {
        val input1 = "Dr. Smith went to St. Louis on Jan. 15th. He met Prof. Adams there."
        val chunks1 = TextSplitter.split(input1, SplitMode.Sentence)
        assertEquals(2, chunks1.size)
        assertEquals("Dr. Smith went to St. Louis on Jan. 15th.", chunks1[0].text)
        assertEquals("He met Prof. Adams there.", chunks1[1].text)

        val input2 = "1. This is the first question explaining the concept. 2. This is the second question."
        val chunks2 = TextSplitter.split(input2, SplitMode.Sentence)
        assertEquals(2, chunks2.size)
        assertEquals("1. This is the first question explaining the concept.", chunks2[0].text)
        assertEquals("2. This is the second question.", chunks2[1].text)
    }

    @Test
    fun testTextSessionTitleAndIntegrity() {
        val session1 = TextSession(sourceText = "Important information for the very first lesson")
        assertEquals("Important information for the…", session1.title)

        val session2 = TextSession(sourceText = "Simple lesson\nContinued details")
        assertEquals("Simple lesson", session2.title)

        val session3 = TextSession(sourceText = "")
        assertEquals("Text", session3.title)
    }
}
