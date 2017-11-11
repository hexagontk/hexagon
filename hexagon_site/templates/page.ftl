
<!DOCTYPE html>

<html lang="en">
<#include "header.ftl">

<body>
  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container-fluid">
      <#include "menu.ftl">
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">
      <!-- 'Status' : 'contribute.html', // TODO Add status section (coverage, metrics, etc.) -->
      <aside class="col-sm-3 col-md-2 sidebar">
        <ul class="nav nav-sidebar">
          <li class="active"><a href="#">Overview <span class="sr-only">(current)</span></a></li>
          <li><a href="#">Reports</a></li>
          <li><a href="#">Analytics</a></li>
          <li><a href="#">Export</a></li>
        </ul>

        <ul class="nav nav-sidebar">
          <li><a href="">Nav item</a></li>
          <li><a href="">Nav item again</a></li>
          <li><a href="">One more nav</a></li>
          <li><a href="">Another nav item</a></li>
          <li><a href="">More navigation</a></li>
        </ul>

        <ul class="nav nav-sidebar">
          <li><a href="">Nav item again</a></li>
          <li><a href="">One more nav</a></li>
          <li><a href="">Another nav item</a></li>
        </ul>
      </aside>

      <main class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
        ${content.body}
      </main>
    </div>

    <div class="row">
      <#include "footer.ftl">
    </div>
  </div>

  <#include "scripts.ftl">
</body>

</html>
