
<#assign bootstrap = "${config.cloudflare}/bootswatch/${config.bootstrapVersion}/${config.theme}" />

<head>
  <meta charset="utf-8" />
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" /><![endif]-->

  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="application-name" content="${config.projectName}" />
  <meta name="author" content="${config.author}" />
  <meta name="description" content="${config.projectDescription}" />
  <meta name="keywords" content="${config.keywords}" />
  <meta name="generator" content="JBake" />

  <meta name="theme-color" content="${config.siteColor}" />
  <meta name="apple-mobile-web-app-status-bar-style" content="${config.siteColor}" />
  <meta name="msapplication-navbutton-color" content="${config.siteColor}" />
  <meta name="msapplication-config" content="/browserconfig.xml">
  <meta name="msapplication-TileColor" content="${config.siteColor}">
  <meta name="msapplication-TileImage" content="${content.rootpath!}apple-touch-icon.png">

  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="mobile-web-app-capable" content="yes" />

  <title>${content.title!Hexagon}</title>

  <link rel="stylesheet" href="${bootstrap}/bootstrap.min.css" />
  <link rel="stylesheet" href="${config.cloudflare}/font-awesome/4.7.0/css/font-awesome.min.css" />

  <link rel="stylesheet" href="${content.rootpath!}css/base.css" />
  <link rel="stylesheet" href="${config.cloudflare}/highlight.js/9.12.0/styles/github.min.css" />

  <link rel="apple-touch-icon" sizes="180x180" href="${content.rootpath!}apple-touch-icon.png" />
  <link rel="shortcut icon" sizes="32x32" href="${content.rootpath!}favicon.ico" />

  <link rel="icon" sizes="16x16 32x32 64x64" href="/favicon.ico">

  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:site" content="@${config.twitterUser}">
  <meta name="twitter:creator" content="@${config.twitterUser}">
  <meta name="twitter:title" content="${config.projectName}">
  <meta name="twitter:description" content="${config.projectDescription}">
  <meta name="twitter:image" content="${config.siteHost}/apple-touch-icon.png">

  <meta property="og:title" content="${config.projectName}" />
  <meta property="og:type" content="article" />
  <meta property="og:url" content="${config.siteHost}" />
  <meta property="og:image" content="${config.siteHost}/apple-touch-icon.png" />
  <meta property="og:description" content="${config.projectDescription}" />

  <meta itemprop="name" content="${config.projectName}">
  <meta itemprop="description" content="${config.projectDescription}">
  <meta itemprop="image" content="${config.siteHost}/apple-touch-icon.png">

  <link rel="author" href="/humans.txt" />
  <link rel="sitemap" type="application/xml" title="Sitemap" href="/sitemap.xml" />
  <link rel="canonical" href="${config.siteHost}/" />

  <!--[if lt IE 9]>
    <script src="${config.cloudflare}/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="${config.cloudflare}/respond.js/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>
