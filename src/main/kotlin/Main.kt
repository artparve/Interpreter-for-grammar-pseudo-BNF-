package source

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import java.io.File

const val PATH = "./src/main/resources/"
fun main() {
    try {
        val fileContent = File(PATH + "input.txt").readText()

        for (i in fileContent.lines()) {
            if (i != "") MyGrammar.parseToEnd(i)
        }
    } catch (e: Exception) {
        for (i in e.toString().split("AlternativesFailure")) {
            println(i.substringAfter("(errors=["))
        }
    }
}
