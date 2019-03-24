
<#assign github = "https://github.com/${config.githubRepo}" />

  <footer class="py-4">
    <div class="container">
      <div class="row">
        <div class="col-4 col-md-2">
          <h3 class="font-weight-bold text-uppercase" data-toc-skip>Usage</h3>
          <ul class="p-0 m-0">
            <#list config.navigationLinks?keys as title>
            <li><a href="${config.navigationLinks[title]}">${title}</a></li>
            </#list>

            <li><a href="${github}/blob/master/README.md">Readme</a></li>
          </ul>
        </div>

        <div class="col-4 col-md-2">
          <h3 class="font-weight-bold text-uppercase" data-toc-skip>Contribute</h3>
          <ul class="p-0 m-0">
            <li><a href="${github}/blob/master/contributing.md">Contributing</a></li>
            <li><a href="${github}/projects/1">Planning</a></li>
            <li><a href="${github}/milestones">Roadmap</a></li>
          </ul>
        </div>

        <div class="col-4 col-md-2">
          <h3 class="font-weight-bold text-uppercase" data-toc-skip>Community</h3>
          <ul class="p-0 m-0">
            <li><a href="${github}">Github</a></li>
            <li><a href="https://twitter.com/${config.twitterUser}">Twitter</a></li>
            <li><a href="${config.slackChannel}">Slack</a></li>
          </ul>
        </div>

        <div class="col-12 col-md-6">
          <div id="credits" class="text-center text-lg-right mt-3 mt-sm-0">
            Made with <i class="fa fa-heart"></i> by <a href="${github}/graphs/contributors">
            OSS contributors</a>. Licensed under <a href="${github}/blob/master/license.md">
            MIT License</a>
          </div>

          <div class="text-center text-lg-right mt-3 mt-sm-2">
            <a
              class="github-button"
              href="${github}"
              data-icon="octicon-star"
              data-show-count="true"
              aria-label="Star ${config.githubRepo} on GitHub">Star</a>
            <a
              class="github-button"
              href="${github}/subscription"
              data-icon="octicon-eye"
              data-show-count="true"
              aria-label="Watch ${config.githubRepo} on GitHub">Watch</a>
            <a
              class="github-button"
              href="${github}/fork"
              data-icon="octicon-git-branch"
              data-show-count="true"
              aria-label="Fork ${config.githubRepo} on GitHub">Fork</a>
            <a
              class="twitter-share-button"
              href="https://twitter.com/share?text=Hexagon Kotlin library">Tweet</a>
          </div>
        </div>
      </div>
    </div>
  </footer>
