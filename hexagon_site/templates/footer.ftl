
  <!-- TODO Show properly on small screens -->
  <footer>
    <div class="container">
      <div class="row">
        <div class="col-xs-2 quicklink-section">
          <div class="footer-title">Documentation</div>
          <ul class="quicklinks">
            <#list config.menu?keys as title>
            <li><a href="${config.menu[title]}">${title}</a></li>
            </#list>
          </ul>
        </div>

        <div class="col-xs-2 quicklink-section">
          <div class="footer-title">Contribute</div>
          <ul class="quicklinks">
            <li>
              <a href="https://github.com/hexagonkt/hexagon/blob/master/contributing.md">
                Contributing
              </a>
            </li>
            <li><a href="https://github.com/hexagonkt/hexagon/projects/1">Planning</a></li>
            <li><a href="https://github.com/hexagonkt/hexagon/milestones">Roadmap</a></li>
          </ul>
        </div>

        <div class="col-xs-2 quicklink-section">
          <div class="footer-title">Community</div>
          <ul class="quicklinks">
            <li><a href="https://github.com/${config.githubRepo}">Github</a></li>
            <li><a href="https://twitter.com/${config.twitterUser}">Twitter</a></li>
            <li><a href="${config.slackChannel}">Slack</a></li>
          </ul>
        </div>

        <div class="col-md-6 col-xs-12 quicklink-section">
          <div class="credits">
            Made with <i class="fa fa-heart heart"></i> by <a href="mailto:${config.authorMail}">
            ${config.author}</a> at
            <a href="https://www.google.com/search?q=madrid+spain">Madrid, Spain</a>. Licensed under
            <a href="https://github.com/hexagonkt/hexagon/blob/master/license.md">MIT License</a>.
          </div>

          <div class="github-buttons">
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
          </div>
        </div>
      </div>
    </div>
  </footer>
