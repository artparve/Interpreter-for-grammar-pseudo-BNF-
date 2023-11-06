/*
package com.example

//import com.example.BooleanGrammar.getValue
//import com.example.BooleanGrammar.provideDelegate
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import kotlin.math.pow
import kotlin.system.exitProcess

//sealed class BooleanExpression
//sealed class StringExpression
sealed class VariableExpression
//sealed class IntExpression
//sealed class Expression
//object TRUE : BooleanExpression()
//object FALSE : BooleanExpression()
data class Variable(val name: String, var value: Int) : VariableExpression()
//data class Number(val value: Int) : IntExpression()

//data class Not(val body: BooleanExpression) : BooleanExpression()
//data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
//data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
//data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
fun customExit(name: String, e: Exception) {
    println(
        "\tError massage - $e\n" +
                "Undefined variable $name\nYou need to define variable before using it!!!!!!!!!!"
    )
}

object MyGrammar : Grammar<Any>() {
    //    val tru by literalToken("true")
    //    val fal by literalToken("false")
    private val varToken by literalToken("var")
    private val equal by literalToken("=")
    private  val mapToken by literalToken("map")
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

    private val varMap = mutableMapOf<String, Variable>()

    //    val not by literalToken("!")
    //    val and by literalToken("&")
    //    val or by literalToken("|")
    private val impl by literalToken("->")
    private val ws by regexToken("\\s+", ignore = true)


    private val number by num use { text.toInt() }

    private val namedVariable by -varToken * parser(this::id) * -equal * parser(this::subSumChain) map {
        Variable(it.t1.text, it.t2).also { newVar ->
            varMap[newVar.name] = newVar
//            println(varMap)
        }
    } or (parser(this::id) * -equal * parser(this::subSumChain) use {
        try {
            varMap[this.t1.text]!!.value = this.t2
            varMap[this.t1.text]!!
        } catch (e: NullPointerException) {
            customExit(this.t1.text, e)
            exitProcess(1)
        }
    })

    private val printText by -print * -doubleQuote * parser(this::id) * -ws * -equal * -ws * -doubleQuote use {
        try {
            println("${this.text} = ${varMap[this.text]!!.value} ")
            0
        } catch (e: NullPointerException) {
            customExit(this.text, e)
            exitProcess(1)
        }
    }

    private val variable by (namedVariable use { value }) or
            (parser(this::id) use {
                try {
                    varMap[text]!!.value
                } catch (e: NullPointerException) {
                    customExit(this.text, e)
                    exitProcess(1)
                }
            })


    private  val term: Parser<Int> by number or
            (skip(minus) and parser(::term) map { -it }) or
            variable or
            (skip(lpar) and parser(::rootParser) and skip(rpar))

    private  val powChain by leftAssociative(term, pow) { a, _, b -> a.toDouble().pow(b.toDouble()).toInt() }

    private  val divMulChain by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        if (op == div) a / b else a * b
    }

    private  val subSumChain by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        if (op == plus) a + b else a - b
    }

    val str: Parser<String> by (skip(print) and skip(doubleQuote) and parser(this::id) and skip(doubleQuote) map {
        println(it.text)
        it.text })

//    private val map by mapToken * lpar * lfigpar * (number or variable)

    private val reduce by

    override val rootParser: Parser<Int> by subSumChain or printText
}

fun main(args: Array<String>) {
//    val expr = """
//        var a = 6 + 5 * 10
//        var g = 10
//        print "g = "
//        var c = g + d
//        print "c = """"
////    for (i in expr.lines()) {
////        if (i != "") println(MyGrammar.parseToEnd(i))
////    }
//    for (i in expr.lines()) {
//        if (i != "") MyGrammar.parseToEnd(i)
//    }
}
*/
