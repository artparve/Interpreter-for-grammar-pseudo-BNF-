import kotlin.math.pow

// Структуры данных для представления AST (абстрактного синтаксического дерева)
sealed class Expr
data class BinaryExpr(val left: Expr, val op: Char, val right: Expr) : Expr()
data class ParenExpr(val expr: Expr) : Expr()
data class IdentifierExpr(val name: String) : Expr()
data class TupleExpr(val first: Expr, val second: Expr) : Expr()
data class NumberExpr(val value: Double) : Expr()
data class MapExpr(val source: Expr, val param: String, val lambda: (Expr, String) -> Expr) : Expr()
data class ReduceExpr(val source: Expr, val initial: Expr, val param1: String, val param2: String, val lambda: (Expr, Expr, String, String) -> Expr) : Expr()

sealed class Stmt
data class VarDeclStmt(val identifier: String, val expr: Expr) : Stmt()
data class OutStmt(val expr: Expr) : Stmt()
data class PrintStmt(val text: String) : Stmt()

// Лексический анализатор
class Lexer(val input: String) {
    var pos = 0

    fun getNextToken(): String {
        while (pos < input.length && input[pos].isWhitespace()) {
            pos++
        }

        if (pos >= input.length) {
            return ""
        }

        if (input[pos] in setOf('(', ')', '{', '}', '+', '-', '*', '/', '^', '=', ',', '-', '>')) {
            return input[pos++].toString()
        }

        if (input[pos].isLetter()) {
            var token = input[pos++].toString()
            while (pos < input.length && input[pos].isLetterOrDigit()) {
                token += input[pos++]
            }
            return token
        }

        if (input[pos].isDigit() || (input[pos] == '.' && pos + 1 < input.length && input[pos + 1].isDigit())) {
            var token = input[pos++].toString()
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                token += input[pos++]
            }
            return token
        }

        if (input.startsWith("map", startIndex = pos)) {
            pos += 3
            return "map"
        }

        if (input.startsWith("reduce", startIndex = pos)) {
            pos += 6
            return "reduce"
        }

        if (input.startsWith("print", startIndex = pos)) {
            pos += 5
            return "print"
        }

        throw IllegalArgumentException("Invalid token at position $pos")
    }
}

// Синтаксический анализатор
class Parser(val lexer: Lexer) {
    private var currentToken: String = ""

    fun parseExpr(): Expr {
        return parseBinaryExpr(0)
    }

    private fun parseBinaryExpr(precedence: Int): Expr {
        var left = parsePrimaryExpr()
        while (true) {
            val token = currentToken
            val tokenPrecedence = getPrecedence(token)
            if (tokenPrecedence < precedence) {
                return left
            }
            lexer.getNextToken() // Consume the operator token
            val right = parseBinaryExpr(tokenPrecedence + 1)
            left = BinaryExpr(left, token[0], right)
        }
    }

    private fun parsePrimaryExpr(): Expr {
        currentToken = lexer.getNextToken()
        when {
            currentToken.isEmpty() -> throw IllegalArgumentException("Unexpected end of input")
            currentToken == "(" -> {
                val expr = parseExpr()
                if (currentToken != ")") {
                    throw IllegalArgumentException("Expected ')' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                return ParenExpr(expr)
            }
            currentToken == "{" -> {
                val first = parseExpr()
                if (currentToken != ",") {
                    throw IllegalArgumentException("Expected ',' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                val second = parseExpr()
                if (currentToken != "}") {
                    throw IllegalArgumentException("Expected '}' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                return TupleExpr(first, second)
            }
            currentToken[0].isLetter() -> {
                val identifier = currentToken
                currentToken = lexer.getNextToken()
                if (currentToken == "(") {
                    currentToken = lexer.getNextToken()
                    val param = currentToken
                    if (currentToken != "->") {
                        throw IllegalArgumentException("Expected '->' at position ${lexer.pos}")
                    }
                    currentToken = lexer.getNextToken()
                    val lambdaBody = parseExpr()
                    if (currentToken != ")") {
                        throw IllegalArgumentException("Expected ')' at position ${lexer.pos}")
                    }
                    currentToken = lexer.getNextToken()
                    return MapExpr(parseExpr(), param) { arg, paramValue -> replaceIdentifier(lambdaBody, param, arg, paramValue) }
                }
                return IdentifierExpr(identifier)
            }
            currentToken[0].isDigit() || currentToken == "-" -> {
                val value = currentToken.toDouble()
                currentToken = lexer.getNextToken()
                return NumberExpr(value)
            }
            else -> throw IllegalArgumentException("Unexpected token '$currentToken' at position ${lexer.pos}")
        }
    }

    private fun replaceIdentifier(expr: Expr, param: String, arg: Expr, paramValue: String): Expr {
        return when (expr) {
            is BinaryExpr -> {
                val left = replaceIdentifier(expr.left, param, arg, paramValue)
                val right = replaceIdentifier(expr.right, param, arg, paramValue)
                BinaryExpr(left, expr.op, right)
            }
            is ParenExpr -> ParenExpr(replaceIdentifier(expr.expr, param, arg, paramValue))
            is IdentifierExpr -> if (expr.name == param) arg else if (expr.name == paramValue) arg else expr
            is TupleExpr -> TupleExpr(replaceIdentifier(expr.first, param, arg, paramValue), replaceIdentifier(expr.second, param, arg, paramValue))
            is MapExpr -> MapExpr(replaceIdentifier(expr.source, param, arg, paramValue), param) { source, paramValue -> replaceIdentifier(expr.lambda(source, paramValue), param, arg, paramValue) }
            is ReduceExpr -> ReduceExpr(
                replaceIdentifier(expr.source, param, arg, paramValue),
                replaceIdentifier(expr.initial, param, arg, paramValue),
                param1,
                param2
            ) { source, initial, paramValue1, paramValue2 ->
                replaceIdentifier(expr.lambda(source, initial, paramValue1, paramValue2), param, arg, paramValue)
            }
            else -> expr
        }
    }

    private fun getPrecedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            else -> 0
        }
    }

    private fun replaceIdentifier(expr: Expr, param: String, value: Expr): Expr {
        return when (expr) {
            is BinaryExpr -> {
                val left = replaceIdentifier(expr.left, param, value)
                val right = replaceIdentifier(expr.right, param, value)
                BinaryExpr(left, expr.op, right)
            }
            is ParenExpr -> ParenExpr(replaceIdentifier(expr.expr, param, value))
            is IdentifierExpr -> if (expr.name == param) value else expr
            is TupleExpr -> TupleExpr(replaceIdentifier(expr.first, param, value), replaceIdentifier(expr.second, param, value))
            is MapExpr -> MapExpr(replaceIdentifier(expr.source, param, value)) { arg ->
                replaceIdentifier(expr.lambda(arg), param, value)
            }
            is ReduceExpr -> ReduceExpr(
                replaceIdentifier(expr.source, param, value),
                replaceIdentifier(expr.initial, param, value)
            ) { acc, elem ->
                replaceIdentifier(expr.lambda(acc, elem), param, value)
            }
            else -> expr
        }
    }

    fun parseStmt(): Stmt {
        currentToken = lexer.getNextToken()
        return when (currentToken) {
            "var" -> {
                currentToken = lexer.getNextToken()
                if (currentToken.isEmpty() || !currentToken[0].isLetter()) {
                    throw IllegalArgumentException("Expected an identifier at position ${lexer.pos}")
                }
                val identifier = currentToken
                currentToken = lexer.getNextToken()
                if (currentToken != "=") {
                    throw IllegalArgumentException("Expected '=' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                val expr = parseExpr()
                VarDeclStmt(identifier, expr)
            }
            "out" -> {
                currentToken = lexer.getNextToken()
                val expr = parseExpr()
                OutStmt(expr)
            }
            "print" -> {
                currentToken = lexer.getNextToken()
                if (currentToken != "\"") {
                    throw IllegalArgumentException("Expected '\"' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                if (currentToken.isEmpty()) {
                    throw IllegalArgumentException("Unexpected end of input")
                }
                val text = currentToken
                currentToken = lexer.getNextToken()
                if (currentToken != "\"") {
                    throw IllegalArgumentException("Expected '\"' at position ${lexer.pos}")
                }
                currentToken = lexer.getNextToken()
                PrintStmt(text)
            }
            else -> throw IllegalArgumentException("Invalid statement at position ${lexer.pos}")
        }
    }

    fun parseProgram(): List<Stmt> {
        val program = mutableListOf<Stmt>()
        while (currentToken.isNotEmpty()) {
            program.add(parseStmt())
        }
        return program
    }
}

// Интерпретатор
fun evalExpr(expr: Expr, env: Map<String, Double>): Double {
    return when (expr) {
        is BinaryExpr -> {
            val leftValue = evalExpr(expr.left, env)
            val rightValue = evalExpr(expr.right, env)
            when (expr.op) {
                '+' -> leftValue + rightValue
                '-' -> leftValue - rightValue
                '*' -> leftValue * rightValue
                '/' -> leftValue / rightValue
                '^' -> leftValue.pow(rightValue)
                else -> throw IllegalArgumentException("Invalid operator: ${expr.op}")
            }
        }
        is ParenExpr -> evalExpr(expr.expr, env)
        is IdentifierExpr -> env[expr.name] ?: throw IllegalArgumentException("Undefined variable: ${expr.name}")
        is TupleExpr -> evalExpr(expr.first, env) + evalExpr(expr.second, env)
        is NumberExpr -> expr.value
        is MapExpr -> {
            val source = evalExpr(expr.source, env)
            val paramValue = env[expr.lambdaParameter] ?: throw IllegalArgumentException("Undefined lambda parameter")
            val lambdaResult = evalExpr(expr.lambda(paramValue), env)
            source + lambdaResult
        }
        is ReduceExpr -> {
            val source = evalExpr(expr.source, env)
            val initial = evalExpr(expr.initial, env)
            val paramValue = env[expr.lambdaParameter] ?: throw IllegalArgumentException("Undefined lambda parameter")
            val lambdaResult = evalExpr(expr.lambda(initial, paramValue), env)
            source + lambdaResult
        }
    }
}

fun main() {
    val input = "var x = 10 out x + 5 print \"Hello, World!\""
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()

    val env = mutableMapOf<String, Double>()

    for (stmt in program) {
        when (stmt) {
            is VarDeclStmt -> {
                val value = evalExpr(stmt.expr, env)
                env[stmt.identifier] = value
            }
            is OutStmt -> {
                val value = evalExpr(stmt.expr, env)
                println(value)
            }
            is PrintStmt -> {
                println(stmt.text)
            }
        }
    }
}
