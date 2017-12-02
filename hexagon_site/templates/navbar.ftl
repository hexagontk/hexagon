
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
        <a class="navbar-brand" href="/">
          <img src="/img/logo_white.svg"> <!-- TODO Add 'Hexagon' for documentation pages only -->
        </a>
      </div>

      <div class="collapse navbar-collapse">
        <ul class="nav navbar-nav navbar-right">
          <#list config.navigationLinks?keys as title>
          <li><a href="${config.navigationLinks[title]}">${title}</a></li>
          </#list>

          <li>
            <a href="https://github.com/${config.githubRepo}">
              <i class="fa fa-github fa-2x" aria-hidden="true" style="font-size: 26px;"></i>
            </a>
          </li>
          <li>
            <a href="https://twitter.com/${config.twitterUser}">
              <i class="fa fa-twitter fa-2x" aria-hidden="true" style="font-size: 26px;"></i>
            </a>
          </li>
          <li>
            <a href="${config.slackChannel}">
              <i class="fa fa-slack fa-2x" aria-hidden="true" style="font-size: 26px;"></i>
            </a>
          </li>
        </ul>
      </div>

