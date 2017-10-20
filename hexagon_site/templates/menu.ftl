
    <!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <a href="https://github.com/${config.githubRepo}" class="hidden-sm hidden-xs">
        <img
          style="position: absolute; top: 0; right: 0; border: 0;"
          src="https://s3.amazonaws.com/github/ribbons/forkme_right_darkblue_121621.png"
          alt="Fork me on GitHub"
          data-canonical-src=
            "https://s3.amazonaws.com/github/ribbons/forkme_right_darkblue_121621.png">
      </a>
      <div class="container">
        <div class="navbar-header">
          <button
            type="button"
            class="navbar-toggle"
            data-toggle="collapse"
            data-target=".navbar-collapse">

            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="pull-left" href="${content.rootpath!}"><img id="logo" src="img/logo.svg"></a>
        </div>

        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <!-- TODO Use config array -->
            <li><a href="${content.rootpath!}index.html">Quick Start</a></li>
            <li><a href="${content.rootpath!}dokka/hexagon/index.html">API</a></li>
            <li><a href="${content.rootpath!}contribute.html">Contribute</a></li>
            <li><a href="${content.rootpath!}license.html">License</a></li>
          </ul>

          <ul class="nav navbar-nav navbar-right">
            <li>
              <a href="https://travis-ci.org/${config.githubRepo}">
                <img
                  src="https://travis-ci.org/${config.githubRepo}.svg?branch=master"
                  alt="BuildImg" />
              </a>
            </li>
            <li>
              <a href="https://codecov.io/github/${config.githubRepo}?branch=master">
                <img
                  src="https://codecov.io/github/${config.githubRepo}/coverage.svg?branch=master"
                  alt="CoverageImg" />
              </a>
            </li>
            <li>
              <a href="https://bintray.com/${config.bintrayRepo}/_latestVersion">
                <img
                  src="https://api.bintray.com/packages/${config.bintrayRepo}/images/download.svg"
                  alt="DownloadImg" />
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

