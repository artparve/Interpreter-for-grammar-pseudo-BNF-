package com.example

//import com.example.BooleanGrammar.getValue
//import com.example.BooleanGrammar.provideDelegate
import kotlin.Exception
import java.lang.NullPointerException
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import java.awt.SystemColor.text
import kotlin.math.pow
import kotlin.system.exitProcess

sealed class BooleanExpression
sealed class StringExpression
sealed class VariableExpression
sealed class IntExpression
sealed class Expression
object TRUE : BooleanExpression()
object FALSE : BooleanExpression()
data class Variable(val name: String, var value: Int) : VariableExpression()
data class Number(val value: Int) : IntExpression()
//data class Not(val body: BooleanExpression) : BooleanExpression()
//data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
//data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
//data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

object MyGrammar : Grammar<Int>() {
    //    val tru by literalToken("true")
//    val fal by literalToken("false")
    val varToken by literalToken("var")
    val equal by literalToken("=")
    val map by literalToken("map")
    val reduce by literalToken("reduce")
    val print by literalToken("print")
    val out by literalToken("by")
    val num by regexToken("-?\\d+")
    val doubleQuote by literalToken("\"")
    val mul by literalToken("*")
    val pow by literalToken("^")
    val div by literalToken("/")
    val minus by literalToken("-")
    val plus by literalToken("+")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val id by regexToken("\\b(?!print\\b)\\w+")
    val newLine by literalToken("\n", ignore = true)

    val varMap = mutableMapOf<String, Variable>()

    //    val not by literalToken("!")
//    val and by literalToken("&")
//    val or by literalToken("|")
    val impl by literalToken("->")
    val ws by regexToken("\\s+", ignore = true)


    val number by num use { text.toInt() }

    val namedVariable by -varToken * parser(this::id) * -equal * parser(this::subSumChain) map {
        Variable(it.t1.text, it.t2).also { newVar -> varMap[newVar.name] = newVar
        println(varMap)
        }
    }
    val variable by (namedVariable use { value }) or (parser(this::id) use { try{varMap[text]!!.value } catch (e: NullPointerException) {
        println("Undefined variable ${this.text}\nYou need to define variable before using it!!!!!!!!!!").also { exitProcess(1) }
        0
    } })

//    val printValue by (-print * -doubleQuote * parser(this::id) * -doubleQuote) map { println(it.text)
//        0}

    //Sasha pridumala napisat parser<String> otdelniy


    val term: Parser<Int> by number or
            (skip(minus) and parser(::term) map { -it }) or
            variable or
            (skip(lpar) and parser(::rootParser) and skip(rpar))

    val powChain by leftAssociative(term, pow) { a, _, b -> a.toDouble().pow(b.toDouble()).toInt() }

    val divMulChain by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        if (op == div) a / b else a * b
    }

    val subSumChain by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        if (op == plus) a + b else a - b
    }

    override val rootParser: Parser<Int> by subSumChain



//    val negation by -not * parser(this::term) map { Not(it) }
//    val bracedExpression by -lpar * parser(this::implChain) * -rpar



//    val term: Parser<BooleanExpression> by
//    (tru asJust TRUE) or
//            (fal asJust FALSE) or
//            (id map { Variable(it.text) }) or
//            negation or
//            bracedExpression

//    val andChain by leftAssociative(term, and) { a, _, b -> And(a, b) }
//    val orChain by leftAssociative(andChain, or) { a, _, b -> Or(a, b) }
//    val implChain by rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) }

//    override val rootParser by implChain
}

fun main(args: Array<String>) {

    val expr = "var a = 6 + 5 * 10"
    val expr1 = "var d = 10"
    val expr3 = "d = 20"
    val expr2 = "var c = a + d"

    println(MyGrammar.parseToEnd(expr))
    println(MyGrammar.parseToEnd(expr1))
    println(MyGrammar.parseToEnd(expr3))
    println(MyGrammar.parseToEnd(expr2))
}
