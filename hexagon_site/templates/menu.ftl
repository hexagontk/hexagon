
    <!-- Fixed navbar -->
    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
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

        <div class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <#list config.menu?keys as title>
            <li><a href="${content.rootpath!}${config.menu[title]}">${title}</a></li>
            </#list>
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
    </nav>

