package source

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import kotlin.math.pow
import kotlin.system.exitProcess

sealed class VariableExpression
sealed class VariableSequenceExpression
data class Variable(val name: String, var value: Double) : VariableExpression()
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

    fun reduce(zeroElement: Double, operation: String): Double {
        var result = zeroElement
        for (element in value) {
            when (operation) {
                "+" -> result += element.toDouble()
                "*" -> result *= element.toDouble()
            }
        }
        return result.toDouble()
    }
}

fun customExit(name: String, e: Exception) {
    println(
        "\tError massage - $e\n" +
                "Undefined variable $name\nYou need to define variable before using it!!!!!!!!!!"
    )
}

fun checkForInt(value: Double): Boolean {
    return value % 1 < 0.01
}

fun convertoInt(value: Double): Int {
    return if (checkForInt(value)) {
        value.toInt()
    } else {
        typemismatch(Double, Int)
        exitProcess(1)
    }
}

fun <T, E> typemismatch(actual: T, expected: E) {
    println("\tType mismatch: expected: $expected, given: $actual")
}


object MyGrammar : Grammar<Any>() {
    private val regexForReduce = """[+*]"""
    private val regexForMap = """->\s*(.+)"""
    private val text by regexToken(""""[^"]*"""")
    private val lamda by regexToken("->\\s+[a-zA-Z\\d+.\\s*^/\\-()]+\\)")
    private val varToken by literalToken("var")
    private val equal by literalToken("=")
    private val mapToken by literalToken("map")
    private val reduceToken by literalToken("reduce")
    private val outputToken by literalToken("out")
    private val print by literalToken("print")
    private val num by regexToken("-?\\d+(\\.\\d+)?")
    private val mul by literalToken("*")
    private val pow by literalToken("^")
    private val div by literalToken("/")
    private val minus by literalToken("-")
    private val plus by literalToken("+")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lfigpar by literalToken("{")
    private val rfigpar by literalToken("}")
    private val id by regexToken("\\w+")
    private val not by literalToken("!")
    private val comma by literalToken(",")
    private val ws by regexToken("\\s+", ignore = true)

    private val varMap = mutableMapOf<String, Variable>()
    private val varSeqMap = mutableMapOf<String, VariableSequence>()

    private fun anyToInt(t: Any): Int {
        return if (t is TokenMatch) {
            try {
                convertoInt(varMap[t.text]!!.value)
            } catch (e: NullPointerException) {
                customExit(t.text, e)
                exitProcess(1)
            }
        } else {
            convertoInt(t as Double)
        }
    }

    private fun seqValToListInt(t1: Any, t2: Any): List<Int> {
        return (anyToInt(t1)..anyToInt(t2)).toList()
    }

    private fun parseMap(variable: String, sequence: VariableSequence, stringForParse: String): VariableSequence {
        val resultOfMap = mutableListOf<Int>()
        val stringForInput = stringForParse.substring(0, stringForParse.length - 1)
        for (item in sequence.value) {
            val parser = MyGrammarSimple.parseToEnd(stringForInput.replace(variable, item.toString()))
            resultOfMap.add(parser)
        }
        return VariableSequence("", resultOfMap)
    }

    private val number by num use { text.toDouble() }

    private val sequenceValue by -lfigpar *
            (number or parser { id }) *
            -comma *
            (number or parser { id }) *
            -rfigpar

    private val namedVariable by
    -varToken * parser { id } * -equal * parser { subSumChain } use {
        Variable(this.t1.text, (this.t2 as Double)).also { newVar ->
            varMap[newVar.name] = newVar
        }
    } or (parser { id } * -equal * parser { subSumChain } use {
        try {
            varMap[this.t1.text]!!.value = (this.t2 as Double)
            varMap[this.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(this.t1.text, e)
            exitProcess(1)
        }
    })

    private val namedVariableSequence by
    -varToken * parser { id } * -equal * (parser { seqVariable } or parser { map }) map {
        VariableSequence(it.t1.text, it.t2.value).also { newVar ->
            varSeqMap[newVar.name] = newVar
        }
    } or (parser { id } * -equal * (parser { seqVariable } or parser { map }) map {
        try {
            varSeqMap[it.t1.text]!!.value = VariableSequence(it.t1.text, it.t2.value)
            varSeqMap[it.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(it.t1.text, e)
            exitProcess(1)
        }
    })


    private val variable by ((namedVariable use { value }) or
            (namedVariableSequence use { this }) or
            (parser(this::id) use {  // already set variable
                try {
                    if (text in varMap.keys) {
                        varMap[text]!!.value
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
            -reduceToken * -lpar * seqVariable * -comma * number * -comma * -id * -id * lamda map {
                val match = regexForReduce.toRegex().find(it.t3.text)
                if (match != null) {
                    val foundSymbol = match.value
                    it.t1.reduce(it.t2, foundSymbol)
                } else {
                    println("Undefined operation")
                    exitProcess(1)
                }
            })

    private val map by -mapToken * -lpar * parser { seqVariable } * -comma * id * lamda map {
        val match = regexForMap.toRegex().find(it.t3.text)
        if (match != null) {
            val capturedText = match.groups[1]!!.value
            parseMap(it.t2.text, it.t1, capturedText)
        } else {
            println("No match found")
            VariableSequence("", listOf())
        }
    }

    //DONE
    private val term: Parser<Any> by number or
            (skip(minus) and parser(::term) map { it }) or
            variable or reduce or
            (skip(lpar) and parser(::rootParser) and skip(rpar))

    //DONE
    private val powChain by leftAssociative(term, pow) { a, _, b ->
        (a as Double).pow(b as Double)
    }

    //DONE
    private val divMulChain by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        if (op == div) (a as Double) / (b as Double) else (a as Double) * (b as Double)
    }

    //DONE
    private val subSumChain by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        if (op == plus) (a as Double) + (b as Double) else (a as Double) - (b as Double)
    }

    // DONE
    private val printText by -print * text use {
        print(this.text.substring(1, this.text.length - 1))
        0
    }

    //DONE
    private val output by -outputToken * parser { id } use {
        try {
            if (this.text in varMap.keys) {
                if (checkForInt(varMap[text]!!.value)) {
                    println(convertoInt(varMap[text]!!.value))
                } else {
                    println(varMap[text]!!.value)
                }
            } else {
                println("${varSeqMap[text]!!.value}")
            }
        } catch (e: NullPointerException) {
            customExit(this.text, e)
            exitProcess(1)
        }
    }

    override val rootParser: Parser<Any> by subSumChain or printText or output
}