
<!DOCTYPE html>

<html lang="en">

<head>
  <meta charset="utf-8"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->

  <meta
    name="viewport"
    content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" />
  <meta name="application-name" content="hexagon" />
  <meta name="author" content="Juanjo Aguililla">
  <meta name="description" content="Hexagon framework web site">
  <meta name="keywords" content="Hexagon, microservices, Java, Kotlin">
  <meta name="generator" content="JBake">

  <title>
    <#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>Hexagon</#if>
  </title>

  <link
    href=
      "${config.bootstrapcdn}/bootswatch/${config.bootstrapVersion}/${config.theme}/bootstrap.min.css"
    rel="stylesheet">
  <link
    rel="stylesheet"
    href="${config.bootstrapcdn}/font-awesome/4.5.0/css/font-awesome.min.css"
    type="text/css">
  <link
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css"
    rel="stylesheet">
  <link href="${config.cloudflare}/highlight.js/9.3.0/styles/default.min.css" rel="stylesheet">

  <!-- Icons -->
  <link rel="apple-touch-icon" sizes="180x180" href="apple-touch-icon.png">
  <link
    rel="apple-touch-icon"
    sizes="180x180"
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>apple-touch-icon.png">
  <link
    rel="shortcut icon"
    sizes="32x32"
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">

  <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
  <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
  <!--[if lt IE 9]>
    <script src="${config.cloudflare}/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="${config.cloudflare}/respond.js/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>

<body onload="prettyPrint()">
  <div id="wrap">

