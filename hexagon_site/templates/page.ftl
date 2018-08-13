
<!DOCTYPE html>

<html lang="en">
<#include "head.ftl">

<body>
  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container-fluid">
      <#include "navbar.ftl">
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">
      <aside class="col-sm-3 col-md-2 sidebar">
        <ul class="nav nav-sidebar">
          <li class="title">Documentation</li>
          <li><a href="/quick_start.html">Quick Start</a></li>
          <li><a href="/motivation.html">Motivation</a></li>
          <li><a href="/alternatives.html">Alternatives</a></li>
          <li><a href="/ports_and_adapters.html">Ports and Adapters</a></li>
          <li><a href="/services.html">Create Services</a></li>
          <li><a href="/building.html">Gradle Helpers</a></li>
          <li><a href="/api.html">API Reference</a></li>
          <li>
            <a href="https://github.com/hexagonkt/hexagon/blob/master/contributing.md">
              Contribute
            </a>
          </li>
        </ul>

        <ul class="nav nav-sidebar">
          <li class="title">Core</li>
          <li><a href="/core/serialization.html">Serialization</a></li>
          <li><a href="/core/configuration.html">Configuration</a></li>
        </ul>

        <ul class="nav nav-sidebar">
          <li class="title">Modules</li>
          <li><a href="/modules/scheduling.html">Scheduling</a></li>
          <li><a href="/modules/testing.html">Testing</a></li>
          <li><a href="/modules/rest.html">REST Helpers</a></li>
        </ul>

        <ul class="nav nav-sidebar">
          <li class="title">Ports</li>
          <li><a href="/ports/server.html">HTTP</a></li>
          <li><a href="/ports/client.html">Client</a></li>
          <li><a href="/ports/storage.html">Storage</a></li>
          <li><a href="/ports/messaging.html">Messaging</a></li>
          <li><a href="/ports/templates.html">Templates</a></li>
        </ul>
      </aside>

      <main class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
        ${content.body}
      </main>
    </div>

    <div class="row">
      <#include "footer.ftl">
    </div>
  </div>

  <#include "scripts.ftl">
</body>

</html>
