
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
        <a class="navbar-brand" href="${content.rootpath!}">
          <img src="img/logo_white.svg">
        </a>
      </div>

      <div class="collapse navbar-collapse">
        <ul class="nav navbar-nav navbar-right">
          <#list config.menu?keys as title>
          <li><a href="${content.rootpath!}${config.menu[title]}">${title}</a></li>
          </#list>

          <li>
            <a href="${config.githubRepo}">
              <i class="fa fa-github fa-2x" aria-hidden="true" style="font-size: 26px;"></i>
            </a>
          </li>
          <li>
            <a href="${config.twitterUser}">
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
    </div>
  </nav>

