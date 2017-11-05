
<!DOCTYPE html>

<html lang="en">
<#include "header.ftl">

<body onload="prettyPrint()">
  <div id="wrap">
    <#include "menu.ftl">

    <div class="container">
      <!-- BODY BEGIN -->
      ${content.body}
      <!-- BODY END -->
    </div>

    <div id="push"></div>
  </div>

  <#include "footer.ftl">
  <#include "scripts.ftl">
</body>

</html>
