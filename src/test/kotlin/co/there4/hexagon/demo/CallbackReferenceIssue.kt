package co.there4.hexagon.demo

/*
 * The following two declarations have the same JVM signature, and yet only the second can be
 * referenced with '::' No matter what the receiver function signature is.
 */
//fun String.aCallback() {}
fun aCallback(receiver: String) {}

fun takeCallback (callback: String.() -> Unit) {}
//fun takeCallback (callback: (String) -> Unit) {}

fun test() {
    takeCallback (::aCallback)
}
