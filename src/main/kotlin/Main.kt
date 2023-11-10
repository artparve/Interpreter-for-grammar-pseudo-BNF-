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
        val primal = """
        var n = 500
var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))
var pi = 4 * reduce(sequence, 0, x y -> x + y)
print "pi = "
out pi"""
        val expr = """
        var t = {1,3}
        var f = reduce(t, 1, x y -> x + y)
        var c = map({1,f}, i -> i * 2 + 10)
        out c
        out f
        print "t = "
        out t"""
        val expr3 = """
            map({1,3}, i -> 2*i)
        """.trimIndent()
        val expr1 = """map({1,n}, i -> 2 * i + 1)"""

        for (i in primal.lines()) {
            if (i != "") MyGrammar.parseToEnd(i)
        }
    } catch (e: Exception) {
        for (i in e.toString().split("AlternativesFailure")) {
            println(i.substringAfter("(errors=["))
        }
    }
     for (i in 0..500) {
         val result = ((-1).toDouble().pow(i.toDouble()) / (2.0 * i + 1))
         println(result)
     }
}
