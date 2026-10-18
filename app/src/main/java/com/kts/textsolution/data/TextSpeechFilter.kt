package com.kts.textsolution.data

enum class SpeechFilterMode {
    None,
    Some,
    Most,
    All,
}

object TextSpeechFilter {
    private val EMOJI_REGEX = Regex(
        // 1. All SMP & SIP & Tag code points (U+10000 to U+10FFFF)
        "[\\x{10000}-\\x{10FFFF}]|" +
        // 2. Keycaps (e.g. 1️⃣, #️⃣, *️⃣)
        "[0-9#*][\\uFE0E\\uFE0F]?[\\u20E3]|" +
        // 3. Misc symbols & Dingbats (❤️, ✂️, ✈️, ☀️, ⚠️, ❌, ✔️, ❓, ❗, ✨, etc.)
        "[\\u2600-\\u27BF]|" +
        // 4. Misc technical (⏰, ⌛, ⌚, ⏩, ⏪, ⏫, ⏬, etc.)
        "[\\u2300-\\u23FF]|" +
        // 5. Misc symbols and arrows (⭐, ⭕, ⬛, ⬜, etc.)
        "[\\u2B00-\\u2BFF]|" +
        // 6. Arrows (⬆️, ⬇️, ⬅️, ➡️, ↗️, ↘️, etc.)
        "[\\u2190-\\u21FF]|" +
        // 7. Supplemental arrows (⤴️, ⤵️, etc.)
        "[\\u2900-\\u297F]|" +
        // 8. Geometric shapes (▶️, ◀️, ⚪, ⚫, etc.)
        "[\\u25A0-\\u25FF]|" +
        // 9. Enclosed Alphanumerics (Ⓜ️, etc.)
        "[\\u2460-\\u24FF]|" +
        // 10. CJK symbols & enclosed (〰️, 〽️, ㊗️, ㊙️, etc.)
        "[\\u3000-\\u303F]|[\\u3200-\\u32FF]|" +
        // 11. General punctuation emojis (‼️, ⁉️)
        "[\\u203C\\u2049]|" +
        // 12. Specific symbol emojis (©️, ®️, ™️, ℹ️)
        "[\\u00A9\\u00AE\\u2122\\u2139][\\uFE0E\\uFE0F]?|" +
        // 13. Variation selectors & Zero Width Joiner & keycap combiner
        "[\\uFE00-\\uFE0F]|[\\u200D]|[\\u20E3]"
    )

    // Repeated decorative characters (2 or more). Exclude '/' so https:// is not broken; handle 3 or more slashes separately.
    private val REPEATED_DECORATIVE = Regex(
        "([\\-*=_~^|\\\\•●■◆▲▼★☆✪✦✧✔✓▫▪▶◄☞👉«»©®™#%<>{}~°·§¶†‡※‹›【】《》「」『』⟨⟩±×÷≠≈≡≤≥∞√]){2,}|/{3,}"
    )

    // Decorative symbols to remove:
    // (a) All Unicode \p{So} (Other Symbols) and \p{Sk} (Modifier Symbols):
    //     includes ° (degree sign), ©, ®, ™, ℃, ℉, №, shapes, arrows, box-drawing borders, etc.
    // (b) Decorative punctuation marks, bullets, brackets, and markdown symbols:
    //     •, ·, §, ¶, †, ‡, ※, ‹, ›, «, », 【, 】, 《, 》, 「, 」, 『, 』, ⟨, ⟩, *, ~, _, ^, |, \, `, ±, ×, ÷, etc.
    private val DECORATIVE_SYMBOLS = Regex(
        "[\\p{So}\\p{Sk}]|" +
        "[•·§¶†‡※‹›«»【】《》「」『』⟨⟩*~_|^\\\\`±×÷≠≈≡≤≥∞√]"
    )

    // Standalone dashes, slashes, or symbols surrounded by whitespace or boundaries
    private val STANDALONE_SYMBOLS = Regex(
        "(?:(?<=\\s)|^)[\\-#+=]+(?=(?:\\s|$))"
    )

    // Normalize repeated punctuation like "???" or "!!!" or "..." to single
    private val EXCESSIVE_PUNCT = Regex(
        "([!?,.]){2,}"
    )

    private val URL_REGEX = Regex(
        "(?i)\\b(?:https?://|ftp://|www\\.)\\S+"
    )

    private val PHONE_REGEX = Regex(
        "(?:\\+?\\d{1,4}[\\s-]?)?(?:\\(?\\d{2,5}\\)?[\\s-]?)?\\d{3,5}[\\s-]?\\d{3,5}"
    )

    private val NUMBER_REGEX = Regex(
        "\\b\\d+[\\d,.:/-]*\\b"
    )

    fun filter(text: String, mode: SpeechFilterMode): String {
        if (text.isBlank()) return text
        if (mode == SpeechFilterMode.All) return text

        return try {
            var result = text

            when (mode) {
                SpeechFilterMode.All -> {
                    return text
                }
                SpeechFilterMode.Most -> {
                    // Keeps text, links, phone numbers, emojis
                    // Cleans repeated decorative symbols / excessive punctuation
                    result = cleanRepeatedSymbols(result)
                }
                SpeechFilterMode.Some -> {
                    // Keeps text, links, phone numbers
                    // Removes emojis, decorative symbols
                    result = removeEmojis(result)
                    result = cleanDecorativeSymbols(result)
                }
                SpeechFilterMode.None -> {
                    // Plain text & numbers
                    // Removes emojis, decorative symbols, URLs, Phone numbers
                    result = removeEmojis(result)
                    result = cleanDecorativeSymbols(result)
                    result = removeUrls(result)
                    result = removePhoneNumbers(result)
                }
            }

            // Clean up excessive whitespace
            result = result.replace(Regex("[ \\t]+"), " ")
                .replace(Regex("(\\r?\\n\\s*){3,}"), "\n\n")
                .trim()

            // Fallback: If filtering made it completely blank, return original text
            if (result.isBlank()) text.trim() else result
        } catch (e: Exception) {
            text.trim()
        }
    }

    private fun removeEmojis(text: String): String =
        EMOJI_REGEX.replace(text, " ")

    private fun cleanRepeatedSymbols(text: String): String {
        var res = REPEATED_DECORATIVE.replace(text, " ")
        res = STANDALONE_SYMBOLS.replace(res, " ")
        res = EXCESSIVE_PUNCT.replace(res, "$1")
        return res
    }

    private fun cleanDecorativeSymbols(text: String): String {
        var res = cleanRepeatedSymbols(text)
        res = DECORATIVE_SYMBOLS.replace(res, " ")
        res = STANDALONE_SYMBOLS.replace(res, " ")
        return res
    }

    private fun removeUrls(text: String): String =
        URL_REGEX.replace(text, " ")

    private fun removePhoneNumbers(text: String): String {
        return PHONE_REGEX.replace(text) { matchResult ->
            val digitsCount = matchResult.value.count { it.isDigit() }
            if (digitsCount in 7..15) " " else matchResult.value
        }
    }

    private fun removeNumbers(text: String): String =
        NUMBER_REGEX.replace(text, " ")
}
