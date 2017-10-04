import com.sysgears.theme.ResourceMapper
import com.sysgears.theme.deploy.GHPagesDeployer
import com.sysgears.theme.taglib.ThemeTagLib

/**
 * Resource mapper and tag libs.
 */
resource_mapper = new ResourceMapper(site).map
tag_libs = [ThemeTagLib]

/**
 * Theme features management.
 */
features {
    /**
     * Defines the highlighting feature. Accepts the following values:
     *  - none - code highlighting is disabled for the theme.
     *  - pygments - code highlighting is enabled and provided by Python Pygments.
     */
    highlight = 'none'

    /**
     * Defines the tool for Markdown documents processing. Accepts the following values:
     * - txtmark - default value. This way TxtMark is used for markdown processing.
     * - pegdown - Use Pegdown for markdown documents processing.
     */
    markdown = 'txtmark'

    /**
     * Defines Compass behavior. This property accepts the following values:
     * - auto, ruby, jruby - Default value. For any of these values the specified Ruby interpreter (ruby.interpreter
     *                       config value)  is used. Otherwise, if no interpreter is defined, falls back to JRuby.
     * - shell - Uses command shell to execute compass.
     * - none - compass is disabled.
     */
    compass = 'none'
}

/**
 * A list of regular expressions that match locations of files or directories that must be completely excluded from processing.
 * These files are ignored by Grain and won't be copied to the destination directory.
 */
excludes += ['/_[^/]*/.*']

/**
 * Defines the set of variables, appended to the 'site' global variable, depending on environment that is used.
 */
environments {

    /**
     * Dev configuration.
     */
    dev {
        log.info 'Development environment is used'

        /**
         * Base URL for the site. This value will be automatically prepended to any asset path of the theme.
         */
        url = "http://localhost:${jetty_port}"

        /**
         * Should posts with "published = false" be processed.
         */
        show_unpublished = true
    }

    /**
     * Prod configuration.
     */
    prod {
        log.info 'Production environment is used'

        /**
         * Base URL for the site. This value will be automatically prepended to any asset path of the theme.
         */
        url = ''

        /**
         * Should posts with "published = false" be processed.
         */
        show_unpublished = false

        /**
         * List of features configurations.
         */
        features {
            minify_xml = false
            minify_html = false
            minify_js = false
            minify_css = false
        }
    }

    /**
     * Theme-specific command-mode environment, used when running a custom command defined in SiteConfig.groovy
     */
    cmd {
        features {
            compass = 'none'
            highlight = 'none'
        }
    }
}

/**
 * Python RPC configuration
 */
python {
    /**
     * An interpreter that is used for executing Python scripts (e.g. for Python Pygments). This property accepts the following values:
     * - python - Uses Python that is installed on your system.
     * - jython - uses Jython integrated in Grain.
     * - auto - Default value. Uses Python that is installed on your system. If its not available, then falls back to Jython.
     */
    interpreter = 'jython'

    /**
     * If native system python distribution is used, then this value defines the paths to python executables. If any of
     * these fails, then the attempt to use next one takes place.
     */
    //cmd_candidates = ['python2', 'python', 'python2.7']

    /**
     * Forces the specific version of Python Setuptools.
     */
    //setup_tools = '2.1'
}

/**
 * Ruby RPC configuration
 */
ruby {
    /**
     * An interpreter that is used used for executing Ruby scripts (e.g. for AsciiDoc and Compass). This property accepts
     * the following values:
     * - ruby - uses Ruby that is installed on your system.
     * - jruby - uses jRuby integrated in Grain.
     * - auto - Default value. Uses Ruby that is installed on your system. If its not available, then falls back to JRuby.
     */
    interpreter = 'jruby'

    /**
     * If native system Ruby distribution is used, then this value defines the paths to Ruby executables. If any of these fails,
     * then the attempt to use next one takes place.
     */
    //cmd_candidates = ['ruby', 'ruby1.8.7', 'ruby1.9.3', 'user.home/.rvm/bin/ruby']

    /**
     * Forces the specific version of Ruby Gems - a tool for managing ruby gems.
     */
    //ruby_gems = '2.2.2'
}

/**
 * Setting this variable to "true" enables prefixing resource relative location with the value
 * of the "site.url" variable.
 */
generate_absolute_links = false

/**
 * A base url to search the post *.markdown files within.
 */
posts_base_url = '/blog/posts/'

/**
 * Blog functionality configuration.
  */
blog {

    /**
     * An email, where a mail from a contact form will be sent.
     */
    contact_email = "your@email.com"

    /**
     * Blog title to be displayed in RSS/Atom feeds.
     */
    title = 'Clean Blog'

    /**
     * The amount of blog posts to be displayed in a feed.
     */
    posts_per_feed = 20

    /**
     * The amount of blog posts to be displayed on a site page.
     */
    posts_per_page = 4
}

/**
 * S3 Deployment configurations.
 *
 * @attr s3bucket - your s3 bucket name
 * @attr deploy_s3 - a command to deploy to Amazon S3.
 */
s3_bucket = ''
deploy_s3 = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"

/**
 * GitHubPages deployment configuration.
 * @attr gh_pages_url Path to GitHub repository in format git@github.com:{username}/{repo}.git
 * @attr deploy a command to deploy to GitHubPages.
 */
gh_pages_url = ''
deploy = new GHPagesDeployer(site).deploy

/**
 * List of custom command-line commands.
  */
commands = [
/**
 * Creates new page. Syntax: ./grainw create-page /path/to/the/page "Page Title"
 *
 * location - relative path to the new page, should start with the /, i.e. /pages/index.html.
 * pageTitle - new page title
 */
'create-page': { String location, String pageTitle ->
        file = new File(content_dir, location)
        file.parentFile.mkdirs()
        file.exists() || file.write("""---
layout: site_page
title: "${pageTitle}"
heading: "${pageTitle}"
image: post-bg.jpg
subheading: ""
published: false
---
""")},

/**
 * Creates new post. Syntax: ./grainw create-post "Post Title"
 *
 * postTitle - new post title
 */
'create-post': { String postTitle ->
    def date = new Date()
    def fileDate = date.format("yyyy-MM-dd")
    def filename = fileDate + "-" + postTitle.encodeAsSlug() + ".markdown"
    def blogDir = new File(content_dir + "${posts_base_url}")
    if (!blogDir.exists()) {
        blogDir.mkdirs()
    }
    def file = new File(blogDir, filename)

    file.exists() || file.write("""---
layout: post
title: "${postTitle}"
subtitle: ""
image: "post-bg.jpg"
date: "${date.format(datetime_format)}"
author: "John Doe"
author_email: ""
author_link: "#"
published: false
---
""")}
]