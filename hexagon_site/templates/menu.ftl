
<#if (content.rootpath)??>
  <#assign root = content.rootpath>
<#else>
  <#assign root = "">
</#if>

    <!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <a href="https://github.com/jaguililla/hexagon" class="hidden-sm hidden-xs">
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
          <a class="pull-left" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>">
            <img id="logo" src="img/logo.svg">
          </a>
        </div>

        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="${root}index.html">Quick Start</a></li>
            <li><a href="${root}dokka/hexagon/index.html">API</a></li>
            <li><a href="${root}contribute.html">Contribute</a></li>
            <li><a href="${root}license.html">License</a></li>
          </ul>

          <ul class="nav navbar-nav navbar-right">
            <li>
              <a href="https://travis-ci.org/jaguililla/hexagon">
                <img
                  src="https://travis-ci.org/jaguililla/hexagon.svg?branch=master"
                  alt="BuildImg" />
              </a>
            </li>
            <li>
              <a href="https://codecov.io/github/jaguililla/hexagon?branch=master">
                <img
                  src="https://codecov.io/github/jaguililla/hexagon/coverage.svg?branch=master"
                  alt="CoverageImg" />
              </a>
            </li>
            <li>
              <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
                <img
                  src=
                    "https://api.bintray.com/packages/jamming/maven/hexagon_core/images/download.svg"
                  alt="DownloadImg" />
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div class="container">
