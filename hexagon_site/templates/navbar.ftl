
      <nav class="navbar navbar-expand-lg navbar-dark fixed-top" role="navigation">
        <div class="container">
          <a class="navbar-brand" href="/">
            <img src="/img/logo_white.svg" height="36" alt="Hexagon Logo">
          </a>

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
            <ul class="navbar-nav ml-auto">
              <#list config.navigationLinks?keys as title>
              <li class="nav-item">
                <a class="nav-link font-weight-bold" href="${config.navigationLinks[title]}">
                  ${title}
                </a>
              </li>
              </#list>

              <li class="nav-item">
                <a class="nav-link font-weight-bold" href="https://github.com/${config.githubRepo}">
                  <i class="fa fa-github" aria-hidden="true"></i>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link font-weight-bold"
                  href="https://twitter.com/${config.twitterUser}">

                  <i class="fa fa-twitter" aria-hidden="true"></i>
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link font-weight-bold" href="${config.slackChannel}">
                  <i class="fa fa-slack" aria-hidden="true"></i>
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>
