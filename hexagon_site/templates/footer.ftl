
  <footer>
    <div class="container">
      <div class="row">
        <div class="col-xs-4 col-md-2">
          <h1>Usage</h1>
          <ul>
            <#list config.navigationLinks?keys as title>
            <li><a href="${config.navigationLinks[title]}">${title}</a></li>
            </#list>

            <li>
              <a href="https://github.com/${config.githubRepo}/blob/master/readme.md">Readme</a>
            </li>
          </ul>
        </div>

        <div class="col-xs-4 col-md-2">
          <h1>Contribute</h1>
          <ul>
            <li>
              <a href="https://github.com/${config.githubRepo}/blob/master/contributing.md">
                Contributing
              </a>
            </li>
            <li><a href="https://github.com/${config.githubRepo}/projects/1">Planning</a></li>
            <li><a href="https://github.com/${config.githubRepo}/milestones">Roadmap</a></li>
          </ul>
        </div>

        <div class="col-xs-4 col-md-2">
          <h1>Community</h1>
          <ul>
            <li><a href="https://github.com/${config.githubRepo}">Github</a></li>
            <li><a href="https://twitter.com/${config.twitterUser}">Twitter</a></li>
            <li><a href="${config.slackChannel}">Slack</a></li>
          </ul>
        </div>

        <div class="col-xs-12 col-md-6">
          <div class="credits">
            Made with <i class="fa fa-heart"></i> by
            <a href="https://github.com/${config.githubRepo}/graphs/contributors">
            OSS contributors</a>. Licensed under
            <a href="https://github.com/${config.githubRepo}/blob/master/license.md">MIT License</a>
          </div>

          <div class="social-buttons">
            <a
              class="github-button"
              href="https://github.com/${config.githubRepo}"
              data-icon="octicon-star"
              data-show-count="true"
              aria-label="Star ${config.githubRepo} on GitHub">Star</a>
            <a
              class="github-button"
              href="https://github.com/${config.githubRepo}/subscription"
              data-icon="octicon-eye"
              data-show-count="true"
              aria-label="Watch ${config.githubRepo} on GitHub">Watch</a>
            <a
              class="github-button"
              href="https://github.com/${config.githubRepo}/fork"
              data-icon="octicon-git-branch"
              data-show-count="true"
              aria-label="Fork ${config.githubRepo} on GitHub">Fork</a>
            <a
              class=
                "twitter-share-button" href="https://twitter.com/share?text=Hexagon Kotlin library">

              Tweet
            </a>
          </div>
        </div>
      </div>
    </div>
  </footer>
