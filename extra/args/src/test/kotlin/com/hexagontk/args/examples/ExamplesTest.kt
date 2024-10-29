package com.hexagontk.args.examples

import com.hexagontk.args.*
import com.hexagontk.args.Property.Companion.HELP
import com.hexagontk.args.Property.Companion.VERSION
import com.hexagontk.core.require

class ExamplesTest {
    private companion object {
        const val serveCommandName: String = "serve"
        const val createCommandName: String = "create"
        const val validateCommandName: String = "validate"

        const val urlParamName: String = "url"
        const val addressParamName: String = "address"
        const val fileParamName: String = "file"
        const val templateOptShortName: Char = 't'
        const val formatOptShortName: Char = 'f'
    }

    internal fun createProgram(buildProperties: Map<String, String>): Program {
        val urlParamDescription = "URL for the CV file to use. If no schema, 'file' is assumed"
        val browseFlag = Flag('b', "browse", "Open browser with served CV")
        val addressParam = Option<String>(
            shortName = 'a',
            name = addressParamName,
            description = "Address to bind the server to",
            Regex("^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!\$)|\$)){4}\$"),
            value = "127.0.0.1"
        )
        val urlParam = Parameter<String>(urlParamName, urlParamDescription, optional = false)

        val serveCommand = Command(
            name = serveCommandName,
            title = "Serve a CV document",
            description = "Serve the CV document supplied, allowing it to be rendered on a browser",
            properties = setOf(HELP, browseFlag, addressParam, urlParam),
        )

        val createCommand = Command(
            name = createCommandName,
            title = "Create a CV document",
            description = "Creates a new CV document based on a template",
            properties = setOf(
                HELP,
                Option<String>(
                    shortName = templateOptShortName,
                    name = "template",
                    description = "Template used to create the new CV",
                    regex = Regex("(regular|full|minimum)"),
                    value = "regular",
                ),
                Option<String>(
                    shortName = formatOptShortName,
                    name = "format",
                    description = "Data format used to store the generated document",
                    regex = Regex("(yaml|toml|json)"),
                    value = "yaml",
                ),
                Parameter<String>(
                    name = fileParamName,
                    description = "File to store the CV document. Document printed on stdout if missed",
                )
            ),
        )

        val validateCommand = Command(
            name = validateCommandName,
            title = "Validate an existing CV",
            description = "Returns a list of errors and a 400 code if the CV document is not valid",
            properties = setOf(HELP, urlParam),
        )

        return Program(
            name = buildProperties.require("project"),
            version = buildProperties.require("version"),
            description = buildProperties.require("description"),
            properties = setOf(VERSION) + serveCommand.properties,
            commands = setOf(serveCommand, createCommand, validateCommand),
        )
    }
}
