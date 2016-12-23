
<!DOCTYPE html>

<html lang="en">

<head>
  <meta charset="utf-8"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->

  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="application-name" content="${config.projectName}" />
  <meta name="author" content="Juanjo Aguililla">
  <meta name="description" content="${config.projectDescription}">
  <meta name="keywords" content="Hexagon, microservices, Java, Kotlin">
  <meta name="generator" content="JBake">

  <meta name="theme-color" content="${config.siteColor}">
  <meta name="msapplication-navbutton-color" content="${config.siteColor}">
  <meta name="apple-mobile-web-app-status-bar-style" content="${config.siteColor}">

  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="mobile-web-app-capable" content="yes">

  <title>
    <#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>Hexagon</#if>
  </title>

  <#assign bootstrapVersion = "3.3.7">
  <#assign theme = "paper">
  <#assign cloudflare = "http://cdnjs.cloudflare.com/ajax/libs">

  <link
    href="${cloudflare}/bootswatch/${bootstrapVersion}/${theme}/bootstrap.min.css"
    rel="stylesheet">
  <link
    rel="stylesheet"
    href="${cloudflare}/font-awesome/4.6.2/css/font-awesome.min.css"
    type="text/css">
  <link
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css"
    rel="stylesheet">
  <link href="${cloudflare}/highlight.js/9.3.0/styles/github.min.css" rel="stylesheet">

  <link
    rel="apple-touch-icon"
    sizes="180x180"
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>apple-touch-icon.png">
  <link
    rel="shortcut icon"
    sizes="32x32"
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">

  <link rel="author" href="/humans.txt" />

  <!--[if lt IE 9]>
    <script src="${cloudflare}/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="${cloudflare}/respond.js/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>

<body onload="prettyPrint()">
  <div id="wrap">

