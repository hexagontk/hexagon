
  <#assign bootstrap = "${config.cloudflare}/twitter-bootstrap/${config.bootstrapVersion}" />

  <!-- Javascript: Placed at the end of the document so the pages load faster -->
  <script src="${config.cloudflare}/jquery/3.2.1/jquery.min.js"></script>
  <script src="${bootstrap}/js/bootstrap.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.12.0/highlight.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.12.0/languages/groovy.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.12.0/languages/gradle.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.12.0/languages/xml.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.12.0/languages/kotlin.min.js"></script>
  <script>hljs.initHighlightingOnLoad();</script>

  <!-- For Github links -->
  <script async defer id="github-bjs" src="https://buttons.github.io/buttons.js"></script>
