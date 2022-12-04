package com.cms.engine.util

class TextJustify {

    companion object {

        /**
         * Very messy and unoptimized implementation of full-justification
         *
         * TODO Respect line breaks
         */
        fun full(text: String, maxWidth: Int): String {
            var width = 0
            var lineWords = ArrayList<String>()
            val lines = ArrayList<ArrayList<String>>()
            val words = text.split(Regex("[ \\t\\n\\r]+"))
            words.forEach { word ->
                // start new line if word cant fit on the current line
                if (maxWidth - width - lineWords.size < word.length) {
                    lines.add(lineWords)
                    lineWords = ArrayList()
                    width = 0
                }
                width += word.length
                lineWords.add(word)
            }
            lines.add(lineWords)

            // go through each group of words and format
            var justified = ""
            lines.forEachIndexed { i, lineWords ->
                var line = ""

                // lines with 1 word OR last word are simply left justified
                // all other lines are left+right justified

                if (i == lines.size-1 || lineWords.size == 1) {
                    line = lineWords.joinToString(" ")
                } else {
                    // construct the line right-to-left, adding spacing
                    // as appropriate to match the desired max length
                    // doing this right-to-left ensures that extra spaces are added
                    // to the left most slots

                    for (j in lineWords.indices.reversed()) {
                        line = if (j == 0) {
                            lineWords[j] + line
                        } else {
                            val numSpaces = numSpacesNeededFullJustify(lineWords.subList(0, j+1), maxWidth - line.length) / j
                            " ".repeat(numSpaces) + lineWords[j] + line
                        }
                    }
                }
                justified += line + "\n"
            }

            return justified
        }

        private fun numSpacesNeededFullJustify(words: List<String>, maxLen: Int): Int {
            var total = 0
            words.forEach {
                total += it.length
            }
            return maxLen - total
        }
    }

}
