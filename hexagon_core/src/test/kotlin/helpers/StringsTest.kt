package com.hexagonkt.helpers

import com.hexagonkt.helpers.AnsiEffect.*
import com.hexagonkt.helpers.AnsiColor.*
import java.util.*
import org.testng.annotations.Test

@Test class StringsTest {
    @Test fun filterVarsExample () {
        val template = "User #{user}"
        val parameters = mapOf<Any, Any>("user" to "John")

        assert (template.filterVars(parameters) == "User John")
        assert (template.filterVars() == template)
    }

    @Test fun `Filter returns the given string if no parameters are set` () {
        val template = "User #{user}"

        assert (template.filterVars(mapOf<Any, Any> ()) == template)
        assert (template.filterVars() == template)
    }

    @Test fun `Filter returns the same string if no variables are defined in it` () {
        val template = "User no vars"

        assert (template.filterVars () == template)
        assert (template.filterVars ("vars" to "value") == template)
        assert (template.filterVars (mapOf<Any, Any> ()) == template)
    }

    @Test fun `Filter returns the same string if variable values are not found` () {
        val template = "User #{user}"

        assert (template.filterVars ("key" to "value") == template)
    }

    @Test fun `Filter ignores empty parameters` () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User #{user} aka #{user} <john@example.co>")
    }

    @Test fun `Filter replaces all occurences of variables with their values` () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "user" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User John aka John <john@example.co>")
    }

    @Test fun `Converting empty text to camel case fails` () {
        assert ("".snakeToCamel () == "")
    }

    @Test fun `Converting valid snake case texts to camel case succeed` () {
        assert ("alfa_beta".snakeToCamel () == "alfaBeta")
        assert ("alfa__beta".snakeToCamel () == "alfaBeta")
        assert ("alfa___beta".snakeToCamel () == "alfaBeta")
    }

    @Test fun `Converting valid camel case texts to snake case succeed` () {
        assert ("alfaBeta".camelToSnake () == "alfa_beta")
    }

    @Test fun `Ansi code without elements returns ansi reset code` () {
        assert (ansi () == "\u001B[0m")
    }

    @Test fun `Ansi code with a single effect has the proper format` () {
        assert (ansi (BOLD) == "\u001B[1m")
        assert (ansi (BOLD_OFF) == "\u001B[21m")
    }

    @Test fun `Ansi code with two effects has the proper format` () {
        assert (ansi (BOLD, UNDERLINE_OFF) == "\u001B[1;24m")
        assert (ansi (BLINK_OFF, INVERSE_OFF) == "\u001B[25;27m")
    }

    @Test fun `Ansi code with foreground and effects returns the correct code` () {
        assert (ansi (RED, BOLD, UNDERLINE) == "\u001B[31;1;4m")
    }

    @Test fun `Ansi code with foreground background and effects returns the correct code` () {
        assert (ansi (RED, BLACK, BLINK, INVERSE) == "\u001B[31;40;5;7m")
    }

    @Test fun `Banner logs the proper message`() {
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
    fun `Rainbow table is printed nicely` () {
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
    fun `Effects and foreground color table`() {
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

    @Test fun `Normalize works as expected`() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assert(striped == "aeiouncAEIOUNC")
    }

    @Test fun `Utf8 returns proper characters`() {
        assert(utf8(0xF0, 0x9F, 0x91, 0x8D) == "👍")
        assert(utf8(0xF0, 0x9F, 0x91, 0x8E) == "👎")
    }
}
