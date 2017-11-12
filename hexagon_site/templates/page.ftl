
<!DOCTYPE html>

<html lang="en">
<#include "header.ftl">

<body>
  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container-fluid">
      <#include "navbar.ftl">
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">
      <aside class="col-sm-3 col-md-2 sidebar">
        <#include "sidebar.ftl">
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
