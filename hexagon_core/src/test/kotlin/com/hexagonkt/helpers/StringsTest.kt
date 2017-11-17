package com.hexagonkt.helpers

import com.hexagonkt.helpers.AnsiEffect.*
import com.hexagonkt.helpers.AnsiColor.*
import java.util.*
import org.testng.annotations.Test

@Test class StringsTest {
    fun filterVarsExample () {
        val template = "User #{user}"
        val parameters = mapOf<Any, Any>("user" to "John")

        assert (template.filterVars(parameters) == "User John")
        assert (template.filterVars() == template)
    }

    fun filter_returns_the_given_string_if_no_parameters_are_set () {
        val template = "User #{user}"

        assert (template.filterVars(mapOf<Any, Any> ()) == template)
        assert (template.filterVars() == template)
    }

    fun filter_returns_the_same_string_if_no_variables_are_defined_in_it () {
        val template = "User no vars"

        assert (template.filterVars () == template)
        assert (template.filterVars ("vars" to "value") == template)
        assert (template.filterVars (mapOf<Any, Any> ()) == template)
    }

    fun filter_returns_the_same_string_if_variable_values_are_not_found () {
        val template = "User #{user}"

        assert (template.filterVars ("key" to "value") == template)
    }

    fun filter_ignores_empty_parameters () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User #{user} aka #{user} <john@example.co>")
    }

    fun filter_replaces_all_occurences_of_variables_with_their_values () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "user" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User John aka John <john@example.co>")
    }

    fun converting_empty_text_to_camel_case_fails () {
        assert ("".snakeToCamel () == "")
    }

    fun converting_valid_snake_case_texts_to_camel_case_succeed () {
        assert ("alfa_beta".snakeToCamel () == "alfaBeta")
    }

    fun converting_valid_camel_case_texts_to_snake_case_succeed () {
        assert ("alfaBeta".camelToSnake () == "alfa_beta")
    }

    fun ansi_code_without_elements_returns_ansi_reset_code () {
        assert (ansi () == "\u001B[0m")
    }

    fun ansi_code_with_a_single_effect_has_the_proper_format () {
        assert (ansi (BOLD) == "\u001B[1m")
        assert (ansi (BOLD_OFF) == "\u001B[21m")
    }

    fun ansi_code_with_two_effects_has_the_proper_format () {
        assert (ansi (BOLD, UNDERLINE_OFF) == "\u001B[1;24m")
        assert (ansi (BLINK_OFF, INVERSE_OFF) == "\u001B[25;27m")
    }

    fun ansi_code_with_foreground_and_effects_returns_the_correct_code () {
        assert (ansi (RED, BOLD, UNDERLINE) == "\u001B[31;1;4m")
    }

    fun ansi_code_with_foreground_background_and_effects_returns_the_correct_code () {
        assert (ansi (RED, BLACK, BLINK, INVERSE) == "\u001B[31;40;5;7m")
    }

    fun banner_logs_the_proper_message() {
        var banner = "alfa line".banner()
        assert(banner.contains("alfa line"))
        assert(banner.contains("*********"))

        banner = "".banner()
        assert(banner == eol + eol)

        banner =
            """alfa
            looong line
            beta
            tango""".trimIndent().trim().banner()
        assert(banner.contains("alfa"))
        assert(banner.contains("beta"))
        assert(banner.contains("tango"))
        assert(banner.contains("looong line"))
        assert(banner.contains("***********"))
    }

    @Test (enabled = false, description = "Only to show the output in a console and check visually")
    fun rainbow_table_is_printed_nicely () {
        println (" %8s | %-8s".format("FORE", "BACK"))

        AnsiColor.values ().forEach { bg ->
            AnsiColor.values ().forEach { fg ->
                print (" %s%8s | %-8s%s".format (ansi (fg, bg), fg, bg, ansi ()))
            }
            println ()
        }

        println ("Back to normal")
    }

    @Test (enabled = false, description = "Only to show the output in a console and check visually")
    fun effects_and_foreground_color_table() {
        println (" %14s | %-14s".format ("FOREGROUND", "EFFECT"))

        AnsiColor.values ().forEach { fg ->
            print (" %s%14s | %-14s%s".format(ansi (fg), fg, "NONE", ansi ()))
            EnumSet.of (BOLD, UNDERLINE, BLINK, INVERSE).forEach { fx ->
                print (" %s%14s | %-14s%s".format (ansi (fg, fx), fg, fx, ansi ()))
            }
            println ()
        }

        println ("Back to normal")
    }

    fun normalize_works_as_expected() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assert(striped == "aeiouncAEIOUNC")
    }

    fun utf8_returns_proper_characters() {
        assert(utf8(0xF0, 0x9F, 0x91, 0x8D) == "👍")
        assert(utf8(0xF0, 0x9F, 0x91, 0x8E) == "👎")
    }
}
