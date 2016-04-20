package co.there4.hexagon.rest.ratpack

import org.testng.annotations.Test
import ratpack.registry.Registry
import ratpack.server.*
import java.net.URI

@Test class RatpackTest {
    fun test_app_from_handler() {
        appFromHandler {
            render("Hello World!")
        }
        .check {
            assert("Hello World!" == text)
        }
    }

    fun test_template_renderer() {
        appFromHandlers {
            get ("hello") {
                template("pebble_template.html", mapOf (
                    "title" to "Greeting",
                    "content" to "Hello World!"
                ))
            }
        }
        .check {
            val body = getBody("hello")
            assert(body.contains ("<title>Greeting</title>"))
            assert(body.contains ("<p>Hello World!</p>"))
            assert(body.contains ("<p>english</p>"))
        }
    }

    fun test_app_from_handlers() {
        appFromHandlers {
            get ("hello") {
                render("Hello World!")
            }
            get ("bye") {
                render("Good bye cruel World!")
            }
        }
        .check {
            assert("Hello World!" == getBody("hello"))
            assert("Good bye cruel World!" == getBody("bye"))
        }
    }

    fun test_app_from_server() {
        val server = serverOf {
            handlers {
                get ("hello") {
                    render("Hello World!")
                }
                get ("bye") {
                    render("Good bye cruel World!")
                }
            }
        }

        appFromServer (server).check {
            assert("Hello World!" == getBody("hello"))
            assert("Good bye cruel World!" == getBody("bye"))
        }
    }

    fun test_server_start() {
        val server = serverStart {
            serverConfig {
                port(5050)
                baseDir(BaseDir.find("logback-test.xml"))
                publicAddress(URI("http://company.org"))
            }
            registry { add("World!") }
            handlers {
                get { render("Hello " + get(String::class.java)) }
                get(":name") { render("Hello ${pathTokens["name"]}!") }
            }
        }

        server.stop()
    }

    fun test_prefix() {
        data class Person(val id: String, val status: String, val age: String)

        appFromHandlers {
            prefix("person/:id") {
                all {
                    val id: String = pathTokens["id"].toString()
                    val person = Person (id, "example-status", "example-age")
                    next(Registry.single(Person::class.java, person))
                }
                get("status") {
                    val person = get(Person::class.java)
                    render("person ${person.id} status: ${person.status}")
                }
                get("age") {
                    val person = get(Person::class.java)
                    render("person ${person.id} age: ${person.age}")
                }
            }
        }
        .check {
            assert("person 10 status: example-status" == getBody("person/10/status"))
            assert("person 6 age: example-age" == getBody("person/6/age"))
        }
    }
}
