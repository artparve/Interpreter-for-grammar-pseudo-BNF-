package source

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import source.MyGrammar.getValue
import source.MyGrammar.provideDelegate
import kotlin.math.min
//import com.github.h0tk3y.betterParse.parser.parse
//import source.MyGrammar.getValue
//import source.MyGrammar.provideDelegate
import kotlin.math.pow
import kotlin.system.exitProcess

sealed class VariableIntExpression
sealed class VariableSequenceExpression
data class VariableInt(val name: String, var value: Int) : VariableIntExpression()
data class VariableSequence(val name: String, var value: List<Int>) : VariableSequenceExpression(), List<Int> {
    override val size: Int
        get() = value.size

    override fun contains(element: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: Int): Int {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<Int> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: Int): Int {
        TODO("Not yet implemented")
    }

    override fun listIterator(): ListIterator<Int> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): ListIterator<Int> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<Int> {
        TODO("Not yet implemented")
    }

    fun reduce(zeroElement: Int, operation: String): Int {
        var result = zeroElement
        for (element in value) {
            when (operation) {
                "+" -> result += element
                "*" -> result *= element
            }
        }
        return result
    }
}

fun customExit(name: String, e: Exception) {
    println(
        "\tError massage - $e\n" +
                "Undefined variable $name\nYou need to define variable before using it!!!!!!!!!!"
    )
}


object MyGrammar : Grammar<Any>() {
    private val text by regexToken(""""[^\"]*"""")
    private val mapLamda by regexToken("""\s*->\s*([a-zA-Z_][a-zA-Z_0-9]*\s*[-+*/%]\s*[a-zA-Z_][a-zA-Z_0-9]*)""") //??????????????????????
    // TODO Генератор регулярных выражений
    private val varToken by literalToken("var")
    private val equal by literalToken("=")
    private val mapToken by literalToken("map")
    private val reduceToken by literalToken("reduce")
    private val outputToken by literalToken("out")
    private val print by literalToken("print")
    //    private val out by literalToken("by")
    private val num by regexToken("-?\\d+")
    private val doubleQuote by literalToken("\"")
    private val impl by literalToken("->")
    private val mul by literalToken("*")
    private val pow by literalToken("^")
    private val div by literalToken("/")
    private val minus by literalToken("-")
    private val plus by literalToken("+")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lfigpar by literalToken("{")
    private val rfigpar by literalToken("}")
    private val id by regexToken("[A-Za-z]+")
    private val not by literalToken("!")
    private val comma by literalToken(",")
    private val ws by regexToken("\\s+", ignore = true)

    val any by zeroOrMore( varToken or equal or mapToken or reduceToken or outputToken or print or num or impl or mul or pow or div or minus or plus or lpar or rpar or lfigpar or rfigpar or id or not or comma or ws )

    private val varIntMap = mutableMapOf<String, VariableInt>()
    private val varSeqMap = mutableMapOf<String, VariableSequence>()
    private val intUnnamed = mutableMapOf<String, VariableInt>()

    private fun seqValToListInt(t1: Any, t2: Any): List<Int> {
        val start = if (t1 is TokenMatch) {
            try {
                varIntMap[t1.text]!!.value
            } catch (e: NullPointerException) {
                customExit(t1.text, e)
                exitProcess(1)
            }
        } else t1 as Int
        val end = if (t2 is TokenMatch) {
            try {
                varIntMap[t2.text]!!.value
            } catch (e: NullPointerException) {
                customExit(t2.text, e)
                exitProcess(1)
            }
        } else t2 as Int
        return (start..end).toList()
    }

    private fun parseMap(variable: String, sequence: VariableSequence, stringForParse: String): VariableSequence {
        val resultOfMap: List<Int> = listOf()
        println(stringForParse)
//        println(stringForParse)
        for (item in sequence.value) {
            val parser = MyGrammarSimple.parseToEnd(stringForParse.replace(variable, item.toString()))
            resultOfMap.plus(parser)
        }
        return VariableSequence("",resultOfMap)
    }

    private val number by num use { text.toInt() }

    private val sequenceValue by -lfigpar *
            (number or parser { id }) *
            -comma *
            (number or parser { id }) *
            -rfigpar

    private val namedVariableInt by
    -literalToken("var") * parser { id } * -equal * parser { subSumChain } use {
        VariableInt(this.t1.text, (this.t2 as Int)).also { newVar ->
            varIntMap[newVar.name] = newVar
//            println(varIntMap)
        }
    } or (parser { id } * -equal * parser { subSumChain } use {
        try {
            varIntMap[this.t1.text]!!.value = (this.t2 as Int)
            varIntMap[this.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(this.t1.text, e)
            exitProcess(1)
        }
    })

    private val namedVariableSequence by
    -literalToken("var") * parser{ id } * -equal * (parser { seqVariable} or parser { map }) map {
        VariableSequence(it.t1.text, it.t2.value).also { newVar ->
            varSeqMap[newVar.name] = newVar
//            println(varSeqMap)
        }
    } or (parser { id } * -equal * (parser { seqVariable} or parser { map }) map {
        try {
            varSeqMap[it.t1.text]!!.value = VariableSequence(it.t1.text, it.t2.value)
            varSeqMap[it.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(it.t1.text, e)
            exitProcess(1)
        }
    })


    private val variable by ((namedVariableInt use { value }) or
            (namedVariableSequence use { this }) or
            (parser(this::id) use {  // already set variable
                try {
                    if (text in varIntMap.keys) {
                        varIntMap[text]!!.value
                    } else {
                        varSeqMap[text]!!
                    }
                } catch (e: NullPointerException) {
                    customExit(this.text, e)
                    exitProcess(1)
                }
            })
            )

    private val seqVariable: Parser<VariableSequence> by (variable use { (this as VariableSequence) }) or
            (sequenceValue map { VariableSequence("", seqValToListInt(it.t1, it.t2)) })

    // reduce ( someSeq , Int , x y -> x operation y )
    private val reduce by (
            -reduceToken * -lpar * seqVariable * -comma * number * -comma * -id * -id * -impl * -id * (plus or mul) * -id * -rpar map {
                it.t1.reduce(it.t2, (it.t3).text)
            })

    private val map by -mapToken * -lpar * parser { seqVariable } * -comma * id * -impl * parser { regexToken("[+\\-*\\d\\w]+") }* -rpar map {
//        parseMap(it.t2.text, it.t1, it.t3.text)

        println(it.t3)
        VariableSequence("t", listOf())
    }

    private val term: Parser<Any> by number or
            (skip(minus) and parser(::term) map { it }) or
            variable or reduce or map or
            (skip(lpar) and parser(::rootParser) and skip(rpar))

    private val powChain by leftAssociative(term, pow) { a, _, b ->
        (a as Int).toDouble().pow((b as Int).toDouble()).toInt()
    }

    private val divMulChain by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        if (op == div) (a as Int) / (b as Int) else (a as Int) * (b as Int)
    }

    private val subSumChain by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        if (op == plus) (a as Int) + (b as Int) else (a as Int) - (b as Int)
    }

    private val func by id * -impl map {

    }

    val str: Parser<String> by (skip(print) and skip(doubleQuote) and parser(this::id) and skip(doubleQuote) map {
        println(it.text)
        it.text })

    private val printTextTestMap by -mapToken * -lpar * parser { seqVariable } * -comma * id * mapLamda * -rpar map  {
        println(it.t3.text)
        0
    }

    // DONE
    private val printText by -print * text use {
        println(this.text.substring(1, this.text.length - 1))
        0
    }

    //DONE
    private val output by -outputToken * parser { id } use {
        try {
            if (this.text in varIntMap.keys) {
                println("${varIntMap[text]!!.value}")
            } else {
                println("${varSeqMap[text]!!.value}")
            }
        } catch (e: NullPointerException) {
            customExit(this.text, e)
            exitProcess(1)
        }
    }

    override val rootParser: Parser<Any> by printTextTestMap // subSumChain or printText or output
}