package com.example.util

import java.text.Collator
import java.util.Locale
import java.util.regex.Pattern

/**
 * Natural order comparator that handles numbers within strings logically.
 * E.g., "01 - Song", "02 - Song", "10 - Song" rather than ASCII "01", "10", "02".
 */
object NaturalOrderComparator : Comparator<String> {
    private val collator: Collator = Collator.getInstance(Locale.getDefault()).apply {
        strength = Collator.SECONDARY
    }
    private val splitPattern = Pattern.compile("(\\d+)|(\\D+)")

    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null && s2 == null) return 0
        if (s1 == null) return -1
        if (s2 == null) return 1

        val matcher1 = splitPattern.matcher(s1)
        val matcher2 = splitPattern.matcher(s2)

        while (matcher1.find() && matcher2.find()) {
            val chunk1 = matcher1.group()
            val chunk2 = matcher2.group()

            val isDigit1 = Character.isDigit(chunk1[0])
            val isDigit2 = Character.isDigit(chunk2[0])

            if (isDigit1 && isDigit2) {
                // Compare numerically
                val num1 = chunk1.toLongOrNull()
                val num2 = chunk2.toLongOrNull()
                if (num1 != null && num2 != null) {
                    val comp = num1.compareTo(num2)
                    if (comp != 0) return comp
                } else {
                    // Fallback for very large numbers
                    val comp = chunk1.length.compareTo(chunk2.length)
                    if (comp != 0) return comp
                    val strComp = chunk1.compareTo(chunk2)
                    if (strComp != 0) return strComp
                }
            } else {
                val comp = collator.compare(chunk1, chunk2)
                if (comp != 0) return comp
            }
        }

        return s1.length.compareTo(s2.length)
    }
}
