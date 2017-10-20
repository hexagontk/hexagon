
<footer class="footer">
  <div id="footerContent" class="container muted credit">
    Made with <i class="fa fa-heart heart"></i> by <a href="http://there4.co">${config.author}</a>
    | Mixed with <a href="http://getbootstrap.com/">Bootstrap v${config.bootstrapVersion}</a>
    | Baked with <a href="http://jbake.org">JBake ${version}</a> |
    <a
      class="github-button"
      href="https://github.com/${config.githubRepo}"
      data-icon="octicon-star"
      data-count-href="/${config.githubRepo}/stargazers"
      data-count-api="/repos/${config.githubRepo}#stargazers_count">Star</a>
    <a
      class="github-button"
      href="https://github.com/${config.githubRepo}"
      data-icon="octicon-eye"
      data-count-href="/${config.githubRepo}/watchers"
      data-count-api="/repos/${config.githubRepo}#subscribers_count"
      data-count-aria-label="# watchers on GitHub"
      aria-label="Watch ${config.githubRepo} on GitHub">Watch</a>
    <a
      class="github-button"
      href="https://github.com/${config.githubRepo}/fork"
      data-icon="octicon-git-branch"
      data-count-href="/${config.githubRepo}/network"
      data-count-api="/repos/${config.githubRepo}#forks_count">Fork</a>
    <a
      class="github-button"
      href="https://github.com/${config.githubRepo}/issues"
      data-icon="octicon-issue-opened"
      data-count-api="/repos/${config.githubRepo}#open_issues_count">Issue</a>
  </div>
</footer>
