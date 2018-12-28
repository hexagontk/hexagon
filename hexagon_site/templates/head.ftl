
<#assign bootstrap = "${config.cloudflare}/bootswatch/${config.bootstrapVersion}/${config.theme}" />
<#assign bootstrapJs = "${config.cloudflare}/twitter-bootstrap/${config.bootstrapVersion}" />

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
  <meta name="msapplication-TileImage" content="/apple-touch-icon.png">

  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="mobile-web-app-capable" content="yes" />

  <title>${content.title!"Hexagon"}</title>

  <link rel="stylesheet" href="${bootstrap}/bootstrap.min.css" />
  <link rel="stylesheet" href="${config.cloudflare}/font-awesome/4.7.0/css/font-awesome.min.css" />

  <link rel="stylesheet" href="/css/base.css" />

  <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png" />
  <link rel="shortcut icon" sizes="32x32" href="/favicon.ico" />
  <link rel="icon" sizes="16x16 32x32 64x64" href="/favicon.ico">

  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:site" content="@${config.twitterUser}">
  <meta name="twitter:creator" content="@${config.twitterUser}">
  <meta name="twitter:title" content="${config.projectName}">
  <meta name="twitter:description" content="${config.longDescription}">
  <meta name="twitter:image" content="${config.siteHost}/apple-touch-icon.png">

  <meta property="og:title" content="${config.projectName}" />
  <meta property="og:type" content="article" />
  <meta property="og:url" content="${config.siteHost}" />
  <meta property="og:image" content="${config.siteHost}/apple-touch-icon.png" />
  <meta property="og:description" content="${config.longDescription}" />

  <meta itemprop="name" content="${config.projectName}">
  <meta itemprop="description" content="${config.longDescription}">
  <meta itemprop="image" content="${config.siteHost}/apple-touch-icon.png">

  <link rel="author" href="/humans.txt" />
  <link rel="sitemap" type="application/xml" title="Sitemap" href="/sitemap.xml" />
  <link rel="canonical" href="${config.siteHost}" />
  <link rel="manifest" href="/manifest.json">

  <!--[if lt IE 9]>
    <script src="${config.cloudflare}/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="${config.cloudflare}/respond.js/1.4.2/respond.min.js"></script>
  <![endif]-->

  <!-- Javascript using 'defer' so the pages load faster -->
  <script defer src="${config.cloudflare}/jquery/${config.jqueryVersion}/jquery.min.js"></script>
  <script defer src="${bootstrapJs}/js/bootstrap.min.js"></script>

  <!-- For share links -->
  <script defer id="github-bjs" src="https://buttons.github.io/buttons.js"></script>
  <script defer src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>

  <!-- Web worker -->
  <script>
  if ('serviceWorker' in navigator)
    navigator.serviceWorker
      .register('/service_worker.js')
      .then(function() { console.log('Worker Registered'); });
  </script>
