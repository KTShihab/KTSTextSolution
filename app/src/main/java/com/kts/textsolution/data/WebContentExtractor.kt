package com.kts.textsolution.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object WebContentExtractor {

    fun isUrl(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.contains('\n')) return false

        // Check single token or starts with http:// or https://
        val parts = trimmed.split(Regex("\\s+"))
        if (parts.size == 1) {
            val lower = parts[0].lowercase()
            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                return true
            }
            if (lower.startsWith("www.") && lower.length > 6 && lower.contains('.')) {
                return true
            }
        }
        return false
    }

    suspend fun extractText(urlString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmed = urlString.trim()
            val validUrl = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                "https://$trimmed"
            } else {
                trimmed
            }

            val doc = Jsoup.connect(validUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .timeout(25000)
                .maxBodySize(0) // Unlimited: do not truncate web page content
                .followRedirects(true)
                .ignoreContentType(true)
                .get()

            // Remove purely non-content or interactive elements
            doc.select("script, style, noscript, svg, canvas, iframe, form, button, input, select, textarea, menu, dialog").remove()
            doc.select(".ads, .ad, .advertisement, #advertisement, .cookie-notice, .cookie-banner, .popup-overlay, .social-share").remove()

            val title = doc.title().trim()

            // Extract text from the body
            val bodyText = extractCleanText(doc.body())

            // Try candidate containers: article, main, content containers
            val candidates = doc.select("article, main, #mw-content-text, .story-content, .article-content, .article-body, .post-content, .entry-content, #content, .content")

            var bestText = bodyText
            // If there is an article/main element that contains most of the content, use it to avoid header/footer clutter
            for (candidate in candidates) {
                val candidateText = extractCleanText(candidate)
                if (candidateText.length >= 250 && candidateText.length >= bodyText.length * 0.45) {
                    bestText = candidateText
                    break
                }
            }

            // Post-process lines: trim lines, collapse multiple empty lines into standard paragraph breaks
            val paragraphs = bestText.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val assembled = paragraphs.joinToString("\n\n")

            val finalText = if (title.isNotBlank() && !assembled.startsWith(title, ignoreCase = true)) {
                "$title\n\n$assembled"
            } else {
                assembled
            }

            if (finalText.isBlank()) {
                Result.failure(Exception("No readable content found"))
            } else {
                Result.success(finalText)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractCleanText(root: Element?): String {
        if (root == null) return ""
        val sb = StringBuilder()

        fun walk(node: Node) {
            when (node) {
                is TextNode -> {
                    val raw = node.text()
                    if (raw.isNotBlank()) {
                        sb.append(raw)
                    } else if (raw.contains(' ') || raw.contains('\t') || raw.contains('\n')) {
                        if (sb.isNotEmpty() && !sb.endsWith(" ") && !sb.endsWith("\n")) {
                            sb.append(" ")
                        }
                    }
                }
                is Element -> {
                    val tag = node.normalName()
                    val isBlock = tag in setOf("p", "div", "section", "article", "header", "footer", "h1", "h2", "h3", "h4", "h5", "h6", "li", "blockquote", "tr", "table", "dt", "dd")
                    val isBr = tag == "br"

                    if (isBlock && sb.isNotEmpty() && !sb.endsWith("\n\n")) {
                        if (sb.endsWith("\n")) sb.append("\n")
                        else sb.append("\n\n")
                    } else if (isBr) {
                        sb.append("\n")
                    }

                    for (child in node.childNodes()) {
                        walk(child)
                    }

                    if (isBlock && sb.isNotEmpty() && !sb.endsWith("\n\n")) {
                        if (sb.endsWith("\n")) sb.append("\n")
                        else sb.append("\n\n")
                    }
                }
            }
        }

        walk(root)
        return sb.toString()
    }
}
