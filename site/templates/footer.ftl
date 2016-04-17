    <div id="push"></div>
  </div>

  <footer class="footer">
    <div id="footerContent" class="container muted credit">
      Made with <i class="fa fa-heart heart"></i> by <a href="http://there4.co">Juanjo Aguililla</a>
      | Mixed with <a href="http://getbootstrap.com/">Bootstrap v${config.bootstrapVersion}</a>
      | Baked with <a href="http://jbake.org">JBake ${version}</a>
    </div>
  </footer>

  <!-- Javascript: Placed at the end of the document so the pages load faster -->
  <script src="${config.cloudflare}/jquery/2.0.3/jquery.min.js"></script>
  <script
    src="${config.cloudflare}/twitter-bootstrap/${config.bootstrapVersion}/js/bootstrap.min.js">
  </script>
  <script src="${config.cloudflare}/highlight.js/9.3.0/highlight.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.3.0/languages/groovy.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.3.0/languages/gradle.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.3.0/languages/xml.min.js"></script>
  <script src="${config.cloudflare}/highlight.js/9.3.0/languages/kotlin.min.js"></script>
  <script>hljs.initHighlightingOnLoad();</script>
  <!-- For Github links -->
  <script async defer id="github-bjs" src="https://buttons.github.io/buttons.js"></script>
</body>
</html>
