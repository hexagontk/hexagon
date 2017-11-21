
<!DOCTYPE html>

<html lang="en">
<#include "head.ftl">

<body>
  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
      <#include "navbar.ftl">
    </div>
  </nav>

  <header>
    <div class="layer">
      <div class="intro container">
        <div class="intro-logo container">
          <img src="img/logo.svg" class="img-responsive center-block"/>
        </div>
        <div class="intro-lead-in">
          <a href="https://travis-ci.org/${config.githubRepo}">
            <img
              src="https://travis-ci.org/${config.githubRepo}.svg?branch=master"
              alt="BuildImg" />
          </a>

          <a href="https://codecov.io/github/${config.githubRepo}?branch=master">
            <img
              src="https://codecov.io/github/${config.githubRepo}/coverage.svg?branch=master"
              alt="CoverageImg" />
          </a>

          <a href="https://codebeat.co/projects/github-com-hexagonkt-hexagon-master">
            <img
              src="https://codebeat.co/badges/f8fafe6f-767a-4248-bc34-e6d4a2acb971"
              alt="Codebeat" />
          </a>

          <a href="https://bintray.com/${config.bintrayRepo}/_latestVersion">
            <img
              src="https://api.bintray.com/packages/${config.bintrayRepo}/images/download.svg"
              alt="DownloadImg" />
          </a>
        </div>
        <div class="intro-lead-in">${config.projectDescription}</div>
        <div class="intro-long">${config.longDescription}</div>
        <a href="/quick_start.html" class="btn btn-xl intro-button">Get Started Now</a>
        <div class="intro-down-arrow">
          <a href="#features"><i class="fa fa-angle-double-down fa-5x" aria-hidden="true"></i></a>
        </div>
      </div>
    </div>
  </header>

  <section id="features">
    <div class="container">
      <div class="row">
        <div class="col-lg-12 text-center">
          <h2 class="section-heading">Features</h2>
          <h3 class="section-subheading text-muted">Hexagon's high-level features.</h3>
        </div>
      </div>

      <!-- First row of features -->
      <div class="row feature-row">
        <#list config.features1 as feature>
        <div class="col-md-4">
          <div class="link">
            <a href="${feature["link"]}">
              <div class="feature-icon">
                <i class="fa fa-${feature["icon"]} fa-4" aria-hidden="true"></i>
              </div>
              <div class="feature-text feature-title">${feature["title"]}</div>
              <div class="feature-text">${feature["description"]}</div>
            </a>
          </div>
        </div>
        </#list>
      </div>

      <!-- Second row of features -->
      <div class="row feature-row">
        <#list config.features2 as feature>
        <div class="col-md-4">
          <div class="link">
            <a href="${feature["link"]}">
              <div class="feature-icon">
                <i class="fa fa-${feature["icon"]} fa-4" aria-hidden="true"></i>
              </div>
              <div class="feature-text feature-title">${feature["title"]}</div>
              <div class="feature-text">${feature["description"]}</div>
            </a>
          </div>
        </div>
        </#list>
      </div>

      <!-- Features button -->
      <div class="row">
        <div class="col-md-12 features-button">
          <a href="/guides.html" class="btn btn-default">See more</a>
        </div>
      </div>
    </div>
  </section>

  <section id="architecture">
    <div class="container">
      <div class="row">
        <div class="col-lg-12 text-center">
          <h2 class="section-heading">Architecture</h2>
          <h3 class="section-subheading text-muted">
            The high level architecture of Hexagon in a picture.
          </h3>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-12 text-center">
          <img
            src="img/architecture.svg"
            class="img-responsive center-block"
            alt="Hexagon architecture diagram" />
        </div>
      </div>
    </div>
  </section>

  <section id="featurematrix">
    <div class="container">
      <div class="row">
        <div class="col-lg-12 text-center">
          <h2 class="section-heading">Ports</h2>
          <h3 class="section-subheading text-muted">Ports with their provided implementations.</h3>
        </div>
      </div>

      <div class="row">
        <div class="col-xs-offset-1 col-xs-10 col-md-offset-2 col-md-8">
          <table class="table">
            <colgroup>
              <col class="col-xs-2 col-md-4">
              <col class="col-xs-4 col-md-4">
            </colgroup>
            <thead>
              <tr>
                <th>Port</th>
                <th>Adapters</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>HTTP</td>
                <td>Undertow, Jetty, Serlvet</td>
              </tr>
              <tr>
                <td>Templates</td>
                <td>Pebble, Rocker</td>
              </tr>
              <tr>
                <td>Serialization formats</td>
                <td>JSON, YAML</td>
              </tr>
              <tr>
                <td>Settings</td>
                <td>Environment, Files, Resources and URLs</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>

  <#include "footer.ftl">
  <#include "scripts.ftl">
</body>

</html>
