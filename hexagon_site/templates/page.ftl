
<!DOCTYPE html>

<html lang="en">

<head>
  <#include "head.ftl">

  <#assign highlight = "${config.cloudflare}/highlight.js/${config.highlightVersion}" />
  <link rel="stylesheet" href="${config.bootstrapToc}/bootstrap-toc.min.css">
  <link rel="stylesheet" href="${highlight}/styles/github.min.css" />
  <link rel="stylesheet" href="/css/page.css" />
</head>

<body
  class="pt-5 text-small"
  data-spy="scroll"
  data-target="#toc"
  data-offset="90"
  onload="hljs.initHighlighting()">

  <#include "navbar.ftl">
  <#assign contributing = "https://github.com/${config.githubRepo}/blob/master/contributing.md" />

  <section class="container-fluid">
    <div class="row">
      <aside class="col-2">
        <nav id="contents" class="sticky-top d-none d-sm-block">
          <h3 class="text-big font-weight-bold">Documentation</h3>
          <ul class="nav navbar-nav">
            <li><a class="nav-link p-0" href="/quick_start.html">Quick Start</a></li>
            <li><a class="nav-link p-0" href="/documentation.html">Documentation</a></li>
            <li><a class="nav-link p-0" href="/alternatives.html">Alternatives</a></li>
            <li><a class="nav-link p-0" href="/gradle.html">Gradle Helpers</a></li>
            <li><a class="nav-link p-0" href="${contributing}">Contribute</a></li>
          </ul>

          <h3 class="text-big font-weight-bold mt-3">Modules</h3>
          <ul class="nav navbar-nav">
            <li><a class="nav-link p-0" href="/hexagon_core/index.html">Core</a></li>
            <li><a class="nav-link p-0" href="/hexagon_scheduler/index.html">Scheduler</a></li>
            <li><a class="nav-link p-0" href="/hexagon_web/index.html">Web</a></li>
          </ul>

          <h3 class="text-big font-weight-bold mt-3">Ports</h3>
          <ul class="nav navbar-nav">
            <li><a class="nav-link p-0" href="/port_http_server/index.html">HTTP</a></li>
            <li><a class="nav-link p-0" href="/port_http_client/index.html">Client</a></li>
            <li><a class="nav-link p-0" href="/port_storage/index.html">Storage</a></li>
            <li><a class="nav-link p-0" href="/port_messaging/index.html">Messaging</a></li>
            <li><a class="nav-link p-0" href="/port_templates/index.html">Templates</a></li>
          </ul>
        </nav>
      </aside>

      <main class="col-md-8">

        ${content.body}

        <div id="disqus_thread" class="mt-5"></div>
      </main>

      <aside class="col-2">
        <nav id="toc" data-toggle="toc" class="sticky-top d-none d-sm-block"></nav>
      </aside>
    </div>
  </section>

  <#include "footer.ftl">

  <script defer src="${config.bootstrapToc}/bootstrap-toc.min.js"></script>
  <script defer src="${highlight}/highlight.min.js"></script>
  <script defer src="${highlight}/languages/groovy.min.js"></script>
  <script defer src="${highlight}/languages/gradle.min.js"></script>
  <script defer src="${highlight}/languages/xml.min.js"></script>
  <script defer src="${highlight}/languages/kotlin.min.js"></script>

  <script>
    var disqus_config = function() {
      this.page.url = '${config.siteHost}/${content.uri}';
      this.page.identifier = '${content.uri}';
    };
    (function() { // DON'T EDIT BELOW THIS LINE
      var d = document, s = d.createElement('script');
      s.src = 'https://${config.disqusCode}.disqus.com/embed.js';
      s.setAttribute('data-timestamp', +new Date());
      (d.head || d.body).appendChild(s);
    })();
  </script>
  <noscript>
    Please enable JavaScript to view the <a href="https://disqus.com/?ref_noscript">comments
    powered by Disqus.</a>
  </noscript>
</body>

</html>
