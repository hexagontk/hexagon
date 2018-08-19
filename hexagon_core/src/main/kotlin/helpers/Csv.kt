package com.hexagonkt.helpers

import java.util.ArrayList

/*
 * https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java
 */

const val DEFAULT_SEPARATOR = ','
const val DEFAULT_QUOTE = '"'

fun parseLine(
    cvsLine: String,
    separators: Char = DEFAULT_SEPARATOR,
    customQuote: Char = DEFAULT_QUOTE): List<String> {

    val result = ArrayList<String>()

    if (cvsLine.isEmpty())
        return result

    var inQuotes = false
    var startCollectChar = false
    var doubleQuotesInColumn = false
    var curVal = StringBuilder()
    val chars = cvsLine.toCharArray()

    for (ch in chars) {
        if (inQuotes) {
            startCollectChar = true

            when (ch) {
                customQuote -> {
                    inQuotes = false
                    doubleQuotesInColumn = false
                }
                // Allow "" in custom quote enclosed
                '"' -> {
                    if (!doubleQuotesInColumn) {
                        curVal.append(ch)
                        doubleQuotesInColumn = true
                    }
                }
                else -> curVal.append(ch)
            }
        }
        else {
            when (ch) {
                customQuote -> {
                    inQuotes = true

                    // Allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '"')
                        curVal.append('"')

                    // Double quotes in column will hit this!
                    if (startCollectChar)
                        curVal.append('"')
                }
                separators -> {
                    result.add(curVal.toString())

                    curVal = StringBuilder()
                    startCollectChar = false
                }
                '\r' -> {} // continue
                '\n' -> {
                    result.add(curVal.toString())
                    return result
                }
                else -> curVal.append(ch)
            }
        }
    }

    result.add(curVal.toString())
    return result
}
