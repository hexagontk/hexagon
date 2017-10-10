package com.sysgears.theme.taglib

import com.sysgears.grain.taglib.GrainTagLib

class ThemeTagLib {

    /**
     * Grain taglib reference.
     */
    private GrainTagLib taglib

    public ThemeTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    /**
     * Converts markdown text to HTML.
     */
    def markdown = { String markdown ->
        String html = [source: markdown ?: "None", markup: 'md'].render().toString()
        html.replaceAll(/(?s)^<p>(.*)<\/p>$/, '$1')
    }

    /**
     * Converts a date to XML date time format: 2013-12-31T12:49:00+07:00.
     *
     * @attr date the date to convert.
     */
    def xmlDateTime = { Map model ->
        if (!model.date) throw new IllegalArgumentException('Tag [xmlDateTime] is missing required attribute [date]')

        def tz = String.format('%tz', model.date)

        String.format("%tFT%<tT${tz.substring(0, 3)}:${tz.substring(3)}", model.date)
    }

    /**
     * Renders a "Posted by [name of a post author, also may be a link to author's page, if provided] on [post creation date].".
     */
    def renderPostDateAndAuthor = { Map post ->
        if (post.author && post.date) {
            def maybePageAuthorLink = (post.author_link) ? "<a href=\"${post.author_link}\">${post.author}</a>" : post.author
            "Posted by " + maybePageAuthorLink + " on " + post.date.format('MMMM dd, yyyy')
        } else ""
    }

    /**
     * Loads configuration bundle represented by .yml files from the specified location and appends it to the
     * "page" object.
     */
    def loadConfigToPage = { String location ->
        taglib.page += loadConfig(location)
    }

    /**
     * Loads configuration file or set of configuration files (if the parameter is folder) and returns it as a map.
     *
     * @param location path to a single .yml configuration file or a folder with .yml files.
     */
    def loadConfig = { location ->
        def config = new File(taglib.site.content_dir as String, location)
        if (config.exists()) {
            if (config.isFile()) {
                return taglib.site.headerParser.parse(config, config.text)
            } else if (config.isDirectory()) {
                def out = [:]
                config.eachFileMatch(~/.*\.yml$/) { file ->
                    out += taglib.site.headerParser.parse(file, file.text)
                }
                return out
            } else [:]
        } else {
            return [:]
        }
    }

    /**
     * Generates html tag for an image
     *
     * @attr location image location
     * @attr width (optional) image width
     * @attr height (optional) image height
     * @attr alt (optional) alternative image text
     * @attr desc (optional) image description (would be displayed under the picture)
     */
    def img = { Map args ->
        String location = args["location"]
        def id = args["id"] ? " id=\"${args["id"]}\"" : ""
        def widthStr = args["width"] ? " width=\"${args["width"]}\"" : ""
        def heightStr = args["height"] ? " height=\"${args["height"]}\"" : ""
        def classStr = args["class"] ? " class=\"${args["class"]}\"" : ""
        def alt = args["alt"] ? args["alt"] : [args["width"], args["height"]].max() < 24 ? " " : "Image"
        def result = "<img${id}${widthStr}${heightStr}${classStr} src=\"${taglib.r(location)}\" alt=\"${alt}\">"
        if (args["desc"]) {
            result += "\n<div><em>${args["desc"]}</em></div>"
        }
        result
    }
}
