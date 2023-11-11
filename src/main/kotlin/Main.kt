package source

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlin.math.pow

fun main(args: Array<String>) {
//    var a = 6 + 5 * 10
//    var g = 10
//    print "g = "
//    var c = g + d
//    print "c = "
    try {
        val expr = """
        var t = {1,3}
        var d = 9.8
        var f = reduce(t, 1, x y -> x + y)
        var c = map({1,f}, i -> i * 2.0 + 10.0  )
        out d
        out c
        out f
        print "t = "
        out t"""

        for (i in expr.lines()) {
            if (i != "") MyGrammar.parseToEnd(i)
        }
    } catch (e: Exception) {
        for (i in e.toString().split("AlternativesFailure")) {
            println(i.substringAfter("(errors=["))
        }
    }
}
