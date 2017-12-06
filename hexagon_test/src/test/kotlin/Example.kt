import com.hexagonkt.client.*
import com.hexagonkt.server.undertow.serve

data class Person (val name: String, val age: Int)

fun main(vararg args: String) {
    serve {
        before { response.addHeader("H1", "val1") }

        get { 400 to "Bad Request" }
        get("/hi") { 200 to "Hello" }
        get("/person") { 200 to Person("Pete", 28) }
    }

    val response = get("http://localhost:9090")
    println("${response.statusCode} > ${response.responseBody}")

    val responseHi = get("http://localhost:9090/hi")
    println("${responseHi.statusCode} > ${responseHi.responseBody}")

    val responsePerson = get("http://localhost:9090/person")
    println("${responsePerson.statusCode} > ${responsePerson.responseBody}")
}
