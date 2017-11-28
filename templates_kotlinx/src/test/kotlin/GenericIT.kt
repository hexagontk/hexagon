
import com.hexagonkt.helpers.Log
import com.hexagonkt.server.Call
import com.hexagonkt.templates.kotlinx.KotlinxEngine.page
import kotlinx.html.*
import javax.script.Compilable
import javax.script.ScriptEngineManager
import java.util.Locale.getDefault as defaultLocale

@Suppress("unused") // Test methods are flagged as unused
class GenericIT {
    private val FORTUNE_MESSAGES = setOf(
        "fortune: No such file or directory",
        "A computer scientist is someone who fixes things that aren't broken.",
        "After enough decimal places, nobody gives a damn.",
        "A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1",
        "A computer program does what you tell it to do, not what you want it to do.",
        "Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen",
        "Any program that runs right is obsolete.",
        "A list is only as strong as its weakest link. — Donald Knuth",
        "Feature: A bug with seniority.",
        "Computers make very fast, very accurate mistakes.",
        "<script>alert(\"This should not be displayed in a browser alert box.\");</script>",
        "フレームワークのベンチマーク"
    )

    fun script () {
        val engine = ScriptEngineManager().getEngineByExtension("kts") as Compilable
        Log.time {
            val compile = engine.compile("val x = 3\nx + 2")
            Log.time {
                println(compile.eval())
            }
        }
    }

    fun Call.initialize() {
        page {
            html {
                head {
                    title { +"Fortunes" }
                }
                body {
                    table {
                        tr {
                            th { +"id" }
                            th { +"message" }
                        }
                        FORTUNE_MESSAGES.forEachIndexed { index, fortune ->
                            tr {
                                td { +index }
                                td { +fortune }
                            }
                        }
                    }
                }
            }
        }
    }
}
