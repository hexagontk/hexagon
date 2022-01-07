package com.hexagonkt.core

import com.hexagonkt.core.helpers.Jvm

/**
 *  Disable heavy and optional checks in runtime. This flag can be enabled to get a small
 *  performance boost. Do *NOT* do this on development (it could mask problems) and enable it on
 *  production only if you have tested your application extensively.
 *
 *  It's initial value is taken from the `DISABLE_CHECKS` flag. See [Jvm.systemFlag] for details on
 *  how flags are checked on a JVM.
 *
 *  This variable can be changed on code to affect only certain parts of the code, however this is
 *  not advised and should be done carefully.
 */
var disableChecks: Boolean = Jvm.systemFlag("DISABLE_CHECKS")
