package co.there4.hexagon.util

/**
 * TODO .
 * To pass a list of causes
 * CodedException (500, "Error", *list)
 *
 * @author jam
 */
class ProgramException(val code: Int, message: String = "", vararg causes: Throwable) :
    RuntimeException (message, causes.firstOrNull()) {

    val causes = causes.toList()
}
