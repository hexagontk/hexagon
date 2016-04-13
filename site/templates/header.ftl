<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>JBake</#if></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="">
  <meta name="author" content="">
  <meta name="keywords" content="">
  <meta name="generator" content="JBake">

  <link
    href="${config.bootstrapcdn}/bootswatch/${config.bootstrapVersion}/${config.theme}/bootstrap.min.css"
    rel="stylesheet">
  <link href="${config.cloudflare}/prettify/r298/prettify.min.css" rel="stylesheet">
  <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css" rel="stylesheet">

  <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
    <script src="${config.cloudflare}/html5shiv/3.7.2/html5shiv.min.js"></script>
  <![endif]-->

  <link
    rel="shortcut icon"
    sizes="32x32"
    href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">
</head>
<body onload="prettyPrint()">
  <div id="wrap">

