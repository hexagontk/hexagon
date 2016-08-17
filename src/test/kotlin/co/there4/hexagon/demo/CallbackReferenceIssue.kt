package co.there4.hexagon.demo

//fun String.doSomething() {}
fun doSomething(receiver: String) {}

fun takeCallback (callback: String.() -> Unit) {}
//fun takeCallback (callback: (String) -> Unit) {}

fun test() {
    takeCallback (::doSomething)
}
