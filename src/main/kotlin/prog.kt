package com.example

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

sealed class VariableIntExpression
sealed class VariableSequenceExpression
data class VariableInt(val name: String, var value: Int) : VariableIntExpression()
data class VariableSequence(val name: String, var value: List<Int>) : VariableSequenceExpression(), List<Int> {
    override val size: Int
        get() = TODO("Not yet implemented")

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
}

fun customExit(name: String, e: Exception) {
    println(
        "\tError massage - $e\n" +
                "Undefined variable $name\nYou need to define variable before using it!!!!!!!!!!"
    )
}

object MyGrammar : Grammar<Any>() {
    private val tru by literalToken("true")
    private val fal by literalToken("false")
    private val varToken by literalToken("var")
    private val equal by literalToken("=")
    private val mapToken by literalToken("map")
    private val reduceToken by literalToken("reduce")
    private val print by literalToken("print")
    private val out by literalToken("by")
    private val num by regexToken("-?\\d+")
    private val doubleQuote by literalToken("\"")
    private val mul by literalToken("*")
    private val pow by literalToken("^")
    private val div by literalToken("/")
    private val minus by literalToken("-")
    private val plus by literalToken("+")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lfigpar by literalToken("{")
    private val rfigpar by literalToken("}")
    private val id by regexToken("\\b(?!print\\b)\\w+")
    private val newLine by literalToken("\n", ignore = true)
    private val not by literalToken("!")
    private val and by literalToken("&")
    private val or by literalToken("|")
    private val impl by literalToken("->")
    private val comma by literalToken(",")
    private val ws by regexToken("\\s+", ignore = true)

    private val varIntMap = mutableMapOf<String, VariableInt>()
    private val varSeqMap = mutableMapOf<String, VariableSequence>()

    private val number by num use { text.toInt() }

    //TODO Полседовательности пока невозможно переопределить

    private val namedVariableInt by -varToken * parser { id } * -equal * parser { subSumChain } map {
        println("Int variable with var")
        VariableInt(it.t1.text, (it.t2 as Int)).also { newVar ->
            varIntMap[newVar.name] = newVar
            println(varIntMap)
        }
    } or (parser { id } * -equal * parser { subSumChain } use {
        println("Int variable without var")
        try {
            varIntMap[this.t1.text]!!.value = (this.t2 as Int)
            varIntMap[this.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(this.t1.text, e)
            exitProcess(1)
        }
    })

    private val namedVariableSequence by -varToken * parser(this::id) * -equal * -lfigpar * (number or parser { id }) * -comma * (number or parser { id }) * -rfigpar map {
        println("namedVariableSequence vis var!!!!!!!!!!!!!!!!!!!!!!")
        when {
            it.t2 is Int && it.t3 is Int -> {
                VariableSequence(it.t1.text, ((it.t2 as Int)..(it.t3 as Int)).toList()).also { newVar ->
                    varSeqMap[newVar.name] = newVar
                    println(varSeqMap)
                }
            }

            it.t2 is Int && it.t3 is TokenMatch -> {
                try {
                    VariableSequence(
                        it.t1.text,
                        ((it.t2 as Int)..varIntMap[(it.t3 as TokenMatch).text]!!.value).toList()
                    ).also { newVar ->
                        varSeqMap[newVar.name] = newVar
                        println(varSeqMap)
                    }
                } catch (e: NullPointerException) {
                    customExit(it.t3.toString(), e)
                    exitProcess(1)
                }
            }

            it.t2 is TokenMatch && it.t3 is Int -> {
                try {
                    VariableSequence(
                        it.t1.text,
                        (varIntMap[(it.t2 as TokenMatch).text]!!.value..(it.t3 as Int)).toList()
                    ).also { newVar ->
                        varSeqMap[newVar.name] = newVar
                        println(varSeqMap)
                    }
                } catch (e: NullPointerException) {
                    customExit(it.t2.toString(), e)
                    exitProcess(1)
                }
            }

            it.t2 is TokenMatch && it.t3 is TokenMatch -> {
                try {
                    VariableSequence(
                        it.t1.text,
                        (varIntMap[(it.t2 as TokenMatch).text]!!.value..varIntMap[(it.t3 as TokenMatch).text]!!.value).toList()
                    ).also { newVar ->
                        varSeqMap[newVar.name] = newVar
                        println(varSeqMap)
                    }
                } catch (e: NullPointerException) {
                    customExit(it.t3.toString(), e)
                    exitProcess(1)
                }
            }

            else -> {
                listOf(0)
            }
        }


    } or (parser { id } * -equal * -lfigpar * (number or parser { id }) * -comma * (number or parser { id }) * -rfigpar map {
        try {
            varSeqMap[it.t1.text]!!.value = when {
                it.t2 is Int && it.t3 is Int -> {
                    VariableSequence(
                        it.t1.text,
                        ((it.t2 as Int)..(it.t3 as Int)).toList()
                    )
                }

                it.t2 is Int && it.t3 is TokenMatch -> {
                    VariableSequence(
                        it.t1.text,
                        ((it.t2 as Int)..varIntMap[(it.t3 as TokenMatch).text]!!.value).toList()
                    )
                }

                it.t2 is TokenMatch && it.t3 is Int -> {
                    VariableSequence(
                        it.t1.text,
                        (varIntMap[(it.t2 as TokenMatch).text]!!.value..(it.t3 as Int)).toList()
                    )
                }

                it.t2 is TokenMatch && it.t3 is TokenMatch -> {
                    VariableSequence(
                        it.t1.text,
                        (varIntMap[(it.t2 as TokenMatch).text]!!.value..varIntMap[(it.t3 as TokenMatch).text]!!.value).toList()
                    )
                }

                else -> {
                    listOf(0)
                }
            }
        } catch (e: NullPointerException) {
            customExit(it.t1.text, e)
            exitProcess(1)
        }
    })


//    or -varToken * parser(this::id) * -equal * -lfigpar * (number or parser(this::id)) * -comma * (number or parser(this::id)) * -rfigpar map  {
//        Variable(it.t1.text, it.t2).also { newVar ->
//            varMap[newVar.name] = newVar
////            println(varMap)
//        }
//    }

//    private val namedVariable by -varToken * parser(this::id) * -equal * parser(this::subSumChain) map {
//            Variable(thitext, this::t2).also { newVar ->
//                varMap[newVar.name] = newVar
////                println(varMap)
//            }
////        } else {
////            try {
////                print((it.t2 as Tuple2<*, *>).t1.)
////                val end = (it.t2 as Tuple2<*, *>).t2
////
//////                println("$start, $end")
////                Variable(it.t1.text, ((((it.t2 as Tuple2<Int, Int>).t1) as Int)..(it.t2 as Tuple2<Int, Int>).t2 as Int).toList()).also { newVar ->
////                    varMap[newVar.name] = newVar
////                    println(varMap)
////                }
////            }catch (e: NullPointerException) {
////                customExit(it.t2.toString(), e)
////                exitProcess(1)
////            }
////
////        }
//    } or ((-lfigpar * (number or parser(this::id)) * -comma * (number or parser(this::id)) * -rfigpar)
//    or (parser(this::id) * -equal * (parser(this::subSumChain) or (-lfigpar * parser(this::id) * -rfigpar)) use {
//        try {
//            varMap[this.t1.text]!!.value = this.t2
//            varMap[this.t1.text]!!
//        } catch (e: NullPointerException) {
//            customExit(this.t1.text, e)
//            exitProcess(1)
//        }
//    })

    private val printText by -print * -doubleQuote * parser(this::id) * -ws * -equal * -ws * -doubleQuote use {
        try {
            println("${this.text} = ${varIntMap[this.text]!!.value} ")
        } catch (e: NullPointerException) {
            try {
                println("${this.text} = ${varSeqMap[this.text]!!.value} ")
            } catch (e: NullPointerException) {
                customExit(this.text, e)
                exitProcess(1)
            }
        }
    }

    private val variable by (namedVariableInt use { value }) or
            (parser(this::id) use {
                try {
                    varIntMap[text]!!.value
                } catch (e: NullPointerException) {
                    customExit(this.text, e)
                    exitProcess(1)
                }
            })

//    private val seqVariable by ()

    private val term: Parser<Any> by number or
            (skip(minus) and parser(::term) map { it }) or
            variable or
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

//    val str: Parser<String> by (skip(print) and skip(doubleQuote) and parser(this::id) and skip(doubleQuote) map {
//        println(it.text)
//        it.text
//    })

//    private val map by mapToken * lpar *

//    private val reduce by -reduceToken *
//            -lpar *
//            (namedVariableSequence or (-lfigpar * (number or parser { id }) * -comma * (number or parser { id }) * -rfigpar)) *
//            -comma *
//            number *
//            parser(this::id) *
//            -impl *
//            parser(this::id) *
//            -rpar map {
//
//    }


    override val rootParser: Parser<Any> by namedVariableSequence or subSumChain or printText
}


fun main(args: Array<String>) {
//    var a = 6 + 5 * 10
//    var g = 10
//    print "g = "
//    var c = g + d
//    print "c = "
    val expr = """
        var b = 0
        var c = 10
        a = {b,c}
        print "a = "
        b = 100
        print "b = "
        a = {10,100}
        print "a = """"
//    for (i in expr.lines()) {
//        if (i != "") println(MyGrammar.parseToEnd(i))
//    }
    for (i in expr.lines()) {
        if (i != "") MyGrammar.parseToEnd(i)
    }
}
