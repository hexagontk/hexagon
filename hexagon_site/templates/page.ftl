
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
      <aside class="col-md-2">
        <nav class="sticky-top hidden-xs">

          <#assign github = "https://github.com/${config.githubRepo}" />
          <h2>Documentation</h2>
          <ul class="nav nav-sidebar">
            <li><a href="/quick_start.html">Quick Start</a></li>
            <li><a href="/documentation.html">Documentation</a></li>
            <li><a href="/alternatives.html">Alternatives</a></li>
            <li><a href="/gradle.html">Gradle Helpers</a></li>
            <li><a href="${github}/blob/master/contributing.md">Contribute</a></li>
          </ul>

          <h2>Modules</h2>
          <ul class="nav nav-sidebar">
            <li><a href="/hexagon_core/index.html">Core</a></li>
            <li><a href="/hexagon_scheduler/index.html">Scheduler</a></li>
            <li><a href="/hexagon_web/index.html">Web</a></li>
          </ul>

          <h2>Ports</h2>
          <ul class="nav nav-sidebar">
            <li><a href="/port_http_server/index.html">HTTP</a></li>
            <li><a href="/port_http_client/index.html">Client</a></li>
            <li><a href="/port_storage/index.html">Storage</a></li>
            <li><a href="/port_messaging/index.html">Messaging</a></li>
            <li><a href="/port_templates/index.html">Templates</a></li>
          </ul>
        </nav>
      </aside>

      <main class="col-md-8">
        ${content.body}
      </main>

      <aside class="col-md-2">
        <nav id="toc" data-toggle="toc" class="sticky-top hidden-xs"></nav>
      </aside>
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
