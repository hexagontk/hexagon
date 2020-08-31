
Major Release Checklist
-----------------------
1. Release site's dependent projects (`store`, `messaging`)
2. Publish their packages using the [Nexus Repository Manager]
3. Merge Hexagon main project to `master` in GitHub
4. Check the site deployment is OK ([https://hexagonkt.com])
5. Publish Hexagon modules using the [Nexus Repository Manager]
6. Update starter repositories (Gradle and Maven ones)
7. Update TFB benchmark
8. Update example projects inside the organization
9. Create a changelog to announce the release
10. Publish changelog on:
 * Dev.to
 * Kotlin Slack
 * Reddit
 * Twitter
 * Kotlin Weekly Newsletter
 * LinkedIn

[Nexus Repository Manager]: https://oss.sonatype.org

Changelog commands
------------------

```shell script
git log 1.2.0...1.3.0 \
  --date=iso8601 \
  --reverse \
  --pretty=format:'* %ad %ar <%an %ae> [View](http://github.com/hexagonkt/hexagon/commit/%H) · %s' \
  >>CHANGELOG.md

git log 1.2.0...1.3.0 --date=iso8601 --reverse --pretty=format:'%an %ae'|sort|uniq >>CHANGELOG.md
```
