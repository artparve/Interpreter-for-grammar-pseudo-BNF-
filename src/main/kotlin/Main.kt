package source

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main(args: Array<String>) {
//    var a = 6 + 5 * 10
//    var g = 10
//    print "g = "
//    var c = g + d
//    print "c = "
    try {
        val expr1 = """
        var t = {1,3}
        var f = reduce(t, 1, x y -> x * y)
        print "f = "
        print "t = "
        out t
        var m = map(t, i -> i * 2)"""
        val expr3 = """
            map({1,3}, i -> 2*i)
        """.trimIndent()
        val expr = """map({1,3}, i -> i * 2)""""

        for (i in expr.lines()) {
            if (i != "") MyGrammar.parseToEnd(i)
        }
    } catch (e: Exception) {
        for (i in e.toString().split("AlternativesFailure")) {
            println(i.substringAfter("(errors=["))
        }
    }


}
