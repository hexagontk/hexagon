
<!DOCTYPE html>

<html lang="en">

<head>
  <#include "head.ftl">

  <#assign highlight = "${config.cloudflare}/highlight.js/${config.highlightVersion}" />
  <link rel="stylesheet" href="${highlight}/styles/shades-of-purple.min.css" />
  <link rel="stylesheet" href="/css/index.css" />
</head>

<body class="pt-5" onload="hljs.initHighlighting()">
  <#include "navbar.ftl">

  <header class="jumbotron">
    <div class="container text-center">
      <img id="logo" class="my-3 my-sm-4" src="/img/logo.svg" alt="Hexagon Logo"/>

      <p>
        <#assign travis = "https://travis-ci.org/${config.githubRepo}" />
        <a href="${travis}">
          <img src="${travis}.svg?branch=master" alt="Travis CI" />
        </a>

        <#assign codecov = "https://codecov.io/github/${config.githubRepo}" />
        <a href="${codecov}?branch=master">
          <img src="${codecov}/coverage.svg?branch=master" alt="Codecov" />
        </a>

        <#assign codebeatBadges = "https://codebeat.co/badges" />
        <a href="https://codebeat.co/projects/github-com-hexagonkt-hexagon-master">
          <img src="${codebeatBadges}/f8fafe6f-767a-4248-bc34-e6d4a2acb971" alt="Codebeat" />
        </a>

        <#assign bintrayPackages = "https://api.bintray.com/packages/${config.bintrayRepo}" />
        <a href="https://bintray.com/${config.bintrayRepo}/_latestVersion">
          <img src="${bintrayPackages}/images/download.svg" alt="Bintray" />
        </a>
      </p>

      <h1 class="my-2">${config.projectDescription}</h1>
      <h3>${config.longDescription}</h3>
      <p><a href="/quick_start.html" class="btn btn-xl">Get Started Now</a></p>
      <a href="#code" aria-hidden="true"><i class="fa fa-angle-double-down fa-5x"></i></a>
    </div>
  </header>

  <section id="code" class="container">
    <div class="row">
      <div class="col-md-12 text-center">
        <h2>Hello World</h2>
        <h3>Simple Hello World HTTP example.</h3>
        <pre style="margin: auto;">
          <code class="language-kotlin hljs">${config.readmeCode}</code>
        </pre>
      </div>
    </div>
  </section>

  <section id="features" class="container">
    <div class="row">
      <div class="col-md-12 text-center">
        <h2>Features</h2>
        <h3>Hexagon's high-level features.</h3>
      </div>
    </div>

    <div class="row">
      <#list config.features as feature>
      <div class="col-md-4 text-center feature">
        <a href="${feature["link"]}">
          <div><i class="fa fa-${feature["icon"]}" aria-hidden="true"></i></div>
          <h2>${feature["title"]}</h2>
          <p>${feature["description"]}</p>
        </a>
      </div>
      </#list>
    </div>

    <div class="row">
      <div class="col-md-12 text-center">
        <a href="/quick_start.html" class="btn btn-default">See more</a>
      </div>
    </div>
  </section>

  <section id="architecture" class="container">
    <div class="row">
      <div class="col-md-12 text-center">
        <h2>Architecture</h2>
        <h3>How Hexagon fits in your architecture in a picture.</h3>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12 text-center">
        <img
          src="/img/architecture.svg"
          class="img-responsive"
          alt="Hexagon architecture diagram" />
      </div>
    </div>
  </section>

  <section id="portAdapters" class="container">
    <div class="row">
      <div class="col-md-12 text-center">
        <h2>Ports</h2>
        <h3>Ports with their provided implementations (Adapters).</h3>
      </div>
    </div>

    <div class="row justify-content-center">
      <div class="col-10 col-md-8">
        <table class="table table-sm w-100">
          <thead class="text-uppercase">
            <tr>
              <th class="border-top-0">Port</th>
              <th class="border-top-0">Adapters</th>
            </tr>
          </thead>
          <tbody>
            <#list config.ports?keys as port>
            <tr>
              <td>${port}</td>
              <td>${config.ports[port]?join(", ")}</td>
            </tr>
            </#list>
          </tbody>
        </table>
      </div>
    </div>
  </section>

  <#include "footer.ftl">

  <script defer src="${highlight}/highlight.min.js"></script>
  <script defer src="${highlight}/languages/kotlin.min.js"></script>
</body>

</html>
