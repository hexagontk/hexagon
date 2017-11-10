
<!DOCTYPE html>

<html lang="en">
<#include "header.ftl">

<body>
  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
      <#include "menu.ftl">
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

          <a href="https://bintray.com/${config.bintrayRepo}/_latestVersion">
            <img
              src="https://api.bintray.com/packages/${config.bintrayRepo}/images/download.svg"
              alt="DownloadImg" />
          </a>
        </div>
        <div class="intro-lead-in">The atoms of your platform</div>
        <div class="intro-long">
          Hexagon is a microservices framework that doesn't follow the flock. It is written in
          Kotlin and its purpose is to ease the building of services (Web applications, APIs or
          queue consumers) that run inside a cloud platform.
        </div>
        <a href="http://hexagonkt.com/quick_start" class="btn btn-xl intro-button">
          Get Started Now
        </a>
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
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Search and filter</div>
                    <div class="feature-text">
                        Combine structured queries and text search to select data.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Advanced ranking</div>
                    <div class="feature-text">
                        Deploy machine learned models for ranking/recommendation.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Organize and aggregate</div>
                    <div class="feature-text">
                        Group and aggregate all data matching queries, in real time.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Realtime</div>
                    <div class="feature-text">
                        UI friendly response times, instant writes at high volume.
                    </div>
                </a>
            </div>
        </div>
      </div>

      <!-- Second row of features -->
      <div class="row feature-row">
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Scalable and fast</div>
                    <div class="feature-text">
                        Optimized for Thousands of queries/sec, billions of documents.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Elastic and fault tolerant</div>
                    <div class="feature-text">
                        Add, remove and replace machines while live and without losing data.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Pluggable</div>
                    <div class="feature-text">
                        Deploy your own Java components to implement custom logic.
                    </div>
                </a>
            </div>
        </div>
        <div class="col-md-3">
            <div class="link">
                <a href="http://hexagonkt.com">
                    <!-- TODO Use Fontawesome -->
                    <div class="feature-icon"></div>
                    <div class="feature-text feature-title">Easy to operate</div>
                    <div class="feature-text">
                        Configure systems with a few lines of text, change anything live.
                     </div>
                </a>
            </div>
        </div>
      </div>

      <!-- Features button -->
      <div class="row">
        <div class="col-md-12 features-button">
          <a href="http://hexagonkt.com" class="btn btn-default">See more</a>
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
            src="img/architecture.png"
            class="img-responsive center-block"
            alt="Hexagon architecture diagram" />
        </div>
      </div>
    </div>
  </section>

  <#include "footer.ftl">
  <#include "scripts.ftl">
</body>

</html>
