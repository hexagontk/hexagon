
<!DOCTYPE html>

<html lang="en">
<#include "head.ftl">

<body data-spy="scroll" data-target="#toc" data-offset="90">

  <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
      <#include "navbar.ftl">
    </div>
  </nav>

  <section class="container">
    <div class="row">
      <div class="col-md-9">
        ${content.body}
      </div>

      <div class="col-md-3">
        <nav id="toc" data-toggle="toc" class="sticky-top"></nav>
      </div>
    </div>
  </section>

  <#include "footer.ftl">

  <#include "scripts.ftl">
</body>

</html>
