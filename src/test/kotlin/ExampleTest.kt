import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import source.MyGrammar
import java.io.File

class ExampleTest {
    val PATH = "./src/test/resources/"

    @Test
    fun easyTest() {
        val input = File(PATH + "input001.txt").readText()
        for (line in input.lines()) {
            MyGrammar.parseToEnd(line)
        }
        val actual = MyGrammar.stringBuilder.toString()
        val expected = File(PATH + "answer001.txt").readText()
        assertEquals(expected, actual)
    }

//    fun t() {
//        val token = Line(Assign("x", 5), Map(2)...)
//        token.execute()
//        token.toString()
//    }
}
