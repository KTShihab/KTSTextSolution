package com.kts.textsolution.data

import java.text.BreakIterator
import java.util.Locale
import java.util.UUID

enum class SplitMode { Paragraph, Sentence, Line, Word, Character }

data class Chunk(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val fontSizeSp: Float? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
)

object TextSplitter {
    fun split(text: String, mode: SplitMode): List<Chunk> {
        val parts = when (mode) {
            SplitMode.Paragraph -> text.split(Regex("\\n+")).map { it.trim() }.filter { it.isNotEmpty() }
            SplitMode.Sentence -> sentences(text)
            SplitMode.Line -> text.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
            SplitMode.Word -> words(text)
            SplitMode.Character -> graphemes(text.replace(Regex("\\s+"), ""))
        }
        return parts.map { Chunk(text = it) }
    }

    fun join(chunks: List<Chunk>, mode: SplitMode?): String {
        val sep = when (mode) {
            SplitMode.Paragraph, null -> "\n\n"
            SplitMode.Line -> "\n"
            SplitMode.Sentence, SplitMode.Word -> " "
            SplitMode.Character -> ""
        }
        return chunks.joinToString(sep) { it.text }
    }

    fun rangeIds(chunks: List<Chunk>, a: String, b: String): List<String> {
        val i = chunks.indexOfFirst { it.id == a }
        val j = chunks.indexOfFirst { it.id == b }
        if (i < 0 || j < 0) return listOf(b)
        val from = minOf(i, j)
        val to = maxOf(i, j)
        return chunks.subList(from, to + 1).map { it.id }
    }

    fun idsToStart(chunks: List<Chunk>, id: String): List<String> {
        val index = chunks.indexOfFirst { it.id == id }
        if (index < 0) return listOf(id)
        return chunks.subList(0, index + 1).map { it.id }
    }

    fun idsToEnd(chunks: List<Chunk>, id: String): List<String> {
        val index = chunks.indexOfFirst { it.id == id }
        if (index < 0) return listOf(id)
        return chunks.subList(index, chunks.size).map { it.id }
    }

    // Common abbreviations in English (case-insensitive)
    private val ENGLISH_ABBREVIATIONS = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "vs", "v", "etc", "st",
        "govt", "ltd", "pvt", "inc", "co", "corp", "gen", "col", "capt", "lt",
        "hon", "rev", "no", "vol", "pp", "p", "fig", "approx", "dept", "est",
        "min", "sec", "hr", "hrs", "jan", "feb", "mar", "apr", "jun", "jul",
        "aug", "sep", "sept", "oct", "nov", "dec", "mon", "tue", "wed", "thu",
        "fri", "sat", "sun", "al", "eg", "ie", "am", "pm",
    )

    private val CLOSING_CHARS = "\"'”’)]}»"

    private fun sentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val raw = segmentSentences(text)
        return mergeShortFragments(raw)
    }

    private fun segmentSentences(text: String): List<String> {
        val result = mutableListOf<String>()
        var start = 0
        var i = 0
        val n = text.length

        while (i < n) {
            val c = text[i]

            // 1. Check for paragraph break (\n\s*\n)
            if (c == '\n') {
                var j = i + 1
                while (j < n && (text[j] == ' ' || text[j] == '\t' || text[j] == '\r')) j++
                if (j < n && text[j] == '\n') {
                    while (j < n && text[j].isWhitespace()) j++
                    val segment = text.substring(start, i).trim()
                    if (segment.isNotEmpty()) result.add(segment)
                    start = j
                    i = j
                    continue
                }
                // Check if next line starts with a bullet point
                if (j < n && (text[j] == '•' || (text[j] == '-' && j + 1 < n && text[j + 1] == ' '))) {
                    val segment = text.substring(start, i).trim()
                    if (segment.isNotEmpty()) result.add(segment)
                    start = j
                    i = j
                    continue
                }
            }

            // 2. Check for strong sentence terminators: ?, !, ।, ॥
            if (c == '?' || c == '!' || c == '؟' || c == '।' || c == '॥') {
                // Consume repeated terminators like ?? or ?!
                var endIdx = i + 1
                while (endIdx < n && (text[endIdx] == '?' || text[endIdx] == '!' || text[endIdx] == '.')) {
                    endIdx++
                }
                // Consume closing quotes/brackets
                while (endIdx < n && CLOSING_CHARS.indexOf(text[endIdx]) >= 0) {
                    endIdx++
                }
                if (endIdx == n || text[endIdx].isWhitespace()) {
                    val segment = text.substring(start, endIdx).trim()
                    if (segment.isNotEmpty()) result.add(segment)
                    while (endIdx < n && text[endIdx].isWhitespace()) endIdx++
                    start = endIdx
                    i = endIdx
                    continue
                }
            }

            // 3. Check for dot '.'
            if (c == '.') {
                if (isSentenceEndDot(text, start, i)) {
                    var endIdx = i + 1
                    while (endIdx < n && text[endIdx] == '.') endIdx++
                    while (endIdx < n && CLOSING_CHARS.indexOf(text[endIdx]) >= 0) {
                        endIdx++
                    }
                    val segment = text.substring(start, endIdx).trim()
                    if (segment.isNotEmpty()) result.add(segment)
                    while (endIdx < n && text[endIdx].isWhitespace()) endIdx++
                    start = endIdx
                    i = endIdx
                    continue
                }
            }

            i++
        }

        if (start < n) {
            val tail = text.substring(start).trim()
            if (tail.isNotEmpty()) result.add(tail)
        }

        return result
    }

    private fun isSentenceEndDot(text: String, sentenceStart: Int, dotIndex: Int): Boolean {
        val n = text.length

        // Decimal numbers like 3.14
        if (dotIndex > 0 && text[dotIndex - 1].isDigit() && dotIndex + 1 < n && text[dotIndex + 1].isDigit()) {
            return false
        }

        // If part of an ellipsis, only consider the last dot
        if (dotIndex + 1 < n && text[dotIndex + 1] == '.') {
            return false
        }

        // Must be followed by end of string, whitespace, or closing quote/bracket then whitespace
        var afterIdx = dotIndex + 1
        while (afterIdx < n && CLOSING_CHARS.indexOf(text[afterIdx]) >= 0) {
            afterIdx++
        }
        if (afterIdx < n && !text[afterIdx].isWhitespace()) {
            return false
        }

        // Extract the word immediately before the dot (or before the ellipsis)
        var wordEnd = dotIndex
        while (wordEnd > sentenceStart && text[wordEnd - 1] == '.') {
            wordEnd--
        }
        var wordStart = wordEnd
        while (wordStart > sentenceStart && !text[wordStart - 1].isWhitespace() && text[wordStart - 1] !in "(\"“'‘[{•") {
            wordStart--
        }
        val prevWord = text.substring(wordStart, wordEnd).trim()

        // Check if preceding word is an abbreviation or initial
        if (isAbbreviationOrInitial(prevWord)) {
            return false
        }

        // Check if the accumulated sentence so far is just a list number or too short (threshold check)
        val candidateSentence = text.substring(sentenceStart, dotIndex + 1).trim()
        if (afterIdx < n) {
            val wordsInCandidate = candidateSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
            // Single word with dot (like an unknown abbreviation/initial) should not split if more text follows
            if (wordsInCandidate.size <= 1) {
                return false
            }
            // Very short candidate (<= 10 chars) should not split on a dot if more text follows
            if (candidateSentence.length <= 10) {
                return false
            }
        }

        return true
    }

    private fun isAbbreviationOrInitial(word: String): Boolean {
        val clean = word.trim().trim('(', ')', '[', ']', '"', '\'', '“', '”', '‘', '’', '•', '-', ',')
            .trimEnd('.')
        if (clean.isEmpty()) return true

        val lowerClean = clean.lowercase(Locale.ROOT)
        if (ENGLISH_ABBREVIATIONS.contains(lowerClean)) return true

        // Single Latin letter initial: A., B., S., H.
        if (clean.length == 1 && clean[0].isLetter()) return true

        // Dotted English acronyms: U.S.A, e.g, i.e
        if (clean.matches(Regex("(?i)([a-z]\\.)+[a-z]?"))) return true

        // Dotted acronyms in any script
        if (clean.contains('.')) {
            val parts = clean.split('.').map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty() && parts.all {
                    ENGLISH_ABBREVIATIONS.contains(it.lowercase(Locale.ROOT)) ||
                        isShortUnicodeInitial(it) ||
                        (it.length == 1 && it[0].isLetter())
                }
            ) {
                return true
            }
        }

        // Short Unicode initial syllable
        if (isShortUnicodeInitial(clean)) return true

        // Numbered list item or Roman numeral: 1., 2., iv.
        if (clean.matches(Regex("\\d+")) || clean.matches(Regex("(?i)[ivxlcdm]+"))) return true

        return false
    }

    private fun isShortUnicodeInitial(token: String): Boolean {
        if (token.isEmpty()) return false
        val isNonAsciiWord = token.all { it.code > 127 && (it.isLetter() || Character.getType(it).toByte() in listOf(Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.FORMAT)) }
        if (!isNonAsciiWord) return false
        return token.length <= 4
    }

    private fun mergeShortFragments(raw: List<String>): List<String> {
        if (raw.size <= 1) return raw
        val result = mutableListOf<String>()
        var pending = ""

        for (chunk in raw) {
            val trimmed = chunk.trim()
            if (trimmed.isEmpty()) continue

            currentStep@ if (pending.isEmpty()) {
                pending = trimmed
            } else {
                pending = "$pending $trimmed"
            }

            if (!shouldMergeWithNext(pending)) {
                result.add(pending)
                pending = ""
            }
        }

        if (pending.isNotEmpty()) {
            if (result.isNotEmpty() && isStandaloneAbbreviation(pending)) {
                val lastIdx = result.lastIndex
                result[lastIdx] = "${result[lastIdx]} $pending"
            } else {
                result.add(pending)
            }
        }

        return result
    }

    private fun shouldMergeWithNext(fragment: String): Boolean {
        val trimmed = fragment.trim()
        if (trimmed.isEmpty()) return false

        // Questions and exclamations can stand on their own if they have at least a word
        if (trimmed.endsWith("?") || trimmed.endsWith("!") || trimmed.endsWith("؟")) {
            return false
        }

        // Incomplete endings must merge with next
        if (trimmed.endsWith(",") || trimmed.endsWith(":") || trimmed.endsWith(";") || trimmed.endsWith("-") || trimmed.endsWith("•")) {
            return true
        }

        if (isStandaloneAbbreviation(trimmed)) {
            return true
        }

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= 1) {
            return true
        }

        val lastWord = words.last()
        if (lastWord.endsWith(".") && isAbbreviationOrInitial(lastWord)) {
            return true
        }

        if (trimmed.length <= 10) {
            return true
        }

        return false
    }

    private fun isStandaloneAbbreviation(text: String): Boolean {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return false
        return words.all { it.endsWith(".") && isAbbreviationOrInitial(it) }
    }

    private fun words(text: String): List<String> {
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    private fun graphemes(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val it = BreakIterator.getCharacterInstance(Locale.getDefault())
        it.setText(text)
        return iterate(it, text).filter { it.isNotBlank() }
    }

    private fun iterate(it: BreakIterator, text: String): List<String> {
        val out = mutableListOf<String>()
        var start = it.first()
        var end = it.next()
        while (end != BreakIterator.DONE) {
            out += text.substring(start, end)
            start = end
            end = it.next()
        }
        return out
    }
}
