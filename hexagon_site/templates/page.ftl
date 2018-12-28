
<!DOCTYPE html>

<html lang="en">

<head>
  <#include "head.ftl">
  <link rel="stylesheet" href="/css/page.css" />
  <link rel="stylesheet" href="${config.bootstrapToc}/bootstrap-toc.min.css">
  <link rel="stylesheet" href="${config.cloudflare}/highlight.js/9.12.0/styles/github.min.css" />
</head>

<body data-spy="scroll" data-target="#toc" data-offset="90" onload="hljs.initHighlighting()">

  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
      <#include "navbar.ftl">
    </div>
  </nav>

  <section class="container">
    <div class="row">
      <div class="col-md-9">
        ${content.body}
      </div>

      <div class="col-md-3">
        <nav id="toc" data-toggle="toc" class="sticky-top"></nav>
      </div>
    </div>
  </section>

  <#include "footer.ftl">

  <#assign highlight = "${config.cloudflare}/highlight.js/${config.highlightVersion}" />

  <script defer src="${config.bootstrapToc}/bootstrap-toc.min.js"></script>
  <script defer src="${highlight}/highlight.min.js"></script>
  <script defer src="${highlight}/languages/groovy.min.js"></script>
  <script defer src="${highlight}/languages/gradle.min.js"></script>
  <script defer src="${highlight}/languages/xml.min.js"></script>
  <script defer src="${highlight}/languages/kotlin.min.js"></script>
</body>

</html>
