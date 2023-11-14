package source

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import java.io.*

const val PATH = "./src/main/resources/"
fun main() {
    try {
        val inputFile = File(PATH + "input.txt").readText()
        val outputFile = File(PATH + "output.txt")
        val myGrammar = MyGrammar()

        FileWriter(outputFile, false).use { writer ->
            for (i in inputFile.lines()) {
                if (i != "") myGrammar.parseToEnd(i)
            }
            writer.write(myGrammar.getResult())
        }

    } catch (e: Exception) {
        for (i in e.toString().split("AlternativesFailure")) {
            println(i.substringAfter("(errors=["))
        }
    } catch (e: FileNotFoundException) {
        // Handle FileNotFoundException
        println("File not found: ${e.message}")
    } catch (e: IOException) {
        // Handle other IOException
        println("IO Exception: ${e.message}")
    } finally {
    }
}
