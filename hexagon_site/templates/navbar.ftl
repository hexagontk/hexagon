
      <nav class="p-2 p-sm-1 navbar navbar-expand-lg navbar-dark fixed-top" role="navigation">
        <div class="container-fluid">
          <a class="navbar-brand" href="/"><img src="/img/logo_white.svg" alt="Hexagon Logo"></a>
          <#if content.fileName??>
          <span class="navbar-brand mb-0 h1">${content.title}</span>
          </#if>

          <button
            class="navbar-toggler"
            type="button"
            data-toggle="collapse"
            data-target="#navbarSupportedContent"
            aria-controls="navbarSupportedContent"
            aria-expanded="false"
            aria-label="Toggle navigation">

            <span class="navbar-toggler-icon"></span>
          </button>

          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <#if content.fileName??>
            <#assign editBranch = "https://github.com/${config.githubRepo}/edit/develop" />
            <#assign editUrl = "${editBranch}/hexagon_site/pages/${content.fileName}" />
            <ul class="navbar-nav font-weight-bold d-none d-sm-block">
              <li class="nav-item">
                <a href="${editUrl}" target="_blank" aria-hidden="true">
                  <i class="fa fa-pencil"></i>
                </a>
              </li>
            </ul>
            </#if>

            <ul class="navbar-nav ml-auto font-weight-bold">
              <#list config.navigationLinks?keys as title>
              <li class="nav-item">
                <a class="nav-link" href="${config.navigationLinks[title]}">${title}</a>
              </li>
              </#list>

              <li class="nav-item">
                <a class="nav-link" href="https://github.com/${config.githubRepo}">
                  <i class="fa fa-github" aria-hidden="true"></i>
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="https://twitter.com/${config.twitterUser}">
                  <i class="fa fa-twitter" aria-hidden="true"></i>
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="${config.slackChannel}">
                  <i class="fa fa-slack" aria-hidden="true"></i>
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>
