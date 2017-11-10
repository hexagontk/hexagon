
  <#assign bootstrap = "${config.cloudflare}/twitter-bootstrap/${config.bootstrapVersion}" />
  <#assign highlight = "${config.cloudflare}/highlight.js/${config.highlightVersion}" />

  <!-- Javascript: Placed at the end of the document so the pages load faster -->
  <script src="${config.cloudflare}/jquery/${config.jqueryVersion}/jquery.min.js"></script>
  <script src="${bootstrap}/js/bootstrap.min.js"></script>
  <script src="${highlight}/highlight.min.js"></script>
  <script src="${highlight}/languages/groovy.min.js"></script>
  <script src="${highlight}/languages/gradle.min.js"></script>
  <script src="${highlight}/languages/xml.min.js"></script>
  <script src="${highlight}/languages/kotlin.min.js"></script>
  <script>hljs.initHighlightingOnLoad();</script>

  <!-- For Github links -->
  <script async defer id="github-bjs" src="https://buttons.github.io/buttons.js"></script>
