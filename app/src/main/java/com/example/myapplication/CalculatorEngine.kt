package com.example.myapplication

/**
 * Парсер выражений методом рекурсивного спуска.
 * Грамматика:
 *   expr   → term (('+' | '-') term)*
 *   term   → factor (('*' | '/') factor)*
 *   factor → unary ('^' unary)*          // правоассоциативно
 *   unary  → postfix | '-' postfix
 *   postfix→ primary ('!')*
 *   primary→ NUMBER | '(' expr ')'
 */
object CalculatorEngine {

    sealed class Token {
        data class Num(val value: Double) : Token()
        data class Op(val op: Char) : Token()   // + - * / ^
        object Factorial : Token()
        object LParen : Token()
        object RParen : Token()
    }

    class CalcException(msg: String) : Exception(msg)

    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        val parser = Parser(tokens)
        val result = parser.parseExpression()
        if (parser.pos < tokens.size) throw CalcException("Некорректное выражение")
        return result
    }

    private fun tokenize(s: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    var seenDot = false
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                        if (s[i] == '.') {
                            if (seenDot) throw CalcException("Двойная точка")
                            seenDot = true
                        }
                        i++
                    }
                    tokens.add(Token.Num(s.substring(start, i).toDouble()))
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' -> {
                    tokens.add(Token.Op(c))
                    i++
                }
                c == '×' -> { tokens.add(Token.Op('*')); i++ }
                c == '÷' -> { tokens.add(Token.Op('/')); i++ }
                c == '!' -> { tokens.add(Token.Factorial); i++ }
                c == '(' -> { tokens.add(Token.LParen); i++ }
                c == ')' -> { tokens.add(Token.RParen); i++ }
                else -> throw CalcException("Неизвестный символ: $c")
            }
        }
        return tokens
    }

    private class Parser(val tokens: List<Token>) {
        var pos = 0

        private fun peek() = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                when ((peek() as? Token.Op)?.op) {
                    '+', '-' -> {
                        val op = (consume() as Token.Op).op
                        val right = parseTerm()
                        left = if (op == '+') left + right else left - right
                    }
                    else -> return left
                }
            }
        }

        private fun parseTerm(): Double {
            var left = parseFactor()
            while (true) {
                when ((peek() as? Token.Op)?.op) {
                    '*', '/' -> {
                        val op = (consume() as Token.Op).op
                        val right = parseFactor()
                        if (op == '*') left *= right
                        else {
                            if (right == 0.0) throw CalcException("Деление на 0")
                            left /= right
                        }
                    }
                    else -> return left
                }
            }
        }

        private fun parseFactor(): Double {
            var base = parseUnary()
            while ((peek() as? Token.Op)?.op == '^') {
                consume()
                val exp = parseUnary() // правоассоциативность: 2^3^2 = 2^(3^2)
                base = Math.pow(base, exp)
            }
            return base
        }

        private fun parseUnary(): Double {
            return if ((peek() as? Token.Op)?.op == '-') {
                consume()
                -parsePostfix()
            } else parsePostfix()
        }

        private fun parsePostfix(): Double {
            var v = parsePrimary()
            while (peek() is Token.Factorial) {
                consume()
                v = factorial(v)
            }
            return v
        }

        private fun parsePrimary(): Double = when (val t = peek()) {
            is Token.Num -> { consume(); t.value }
            Token.LParen -> {
                consume()
                val v = parseExpression()
                if (peek() !is Token.RParen) throw CalcException("Нет закрывающей скобки")
                consume()
                v
            }
            null -> throw CalcException("Неожиданный конец")
            else -> throw CalcException("Неожиданный токен: $t")
        }

        private fun factorial(x: Double): Double {
            if (x < 0 || x != Math.floor(x)) throw CalcException("Факториал: только целые ≥ 0")
            if (x > 170) throw CalcException("Слишком большое число для факториала")
            var r = 1.0
            var n = x.toInt()
            while (n > 1) { r *= n; n-- }
            return r
        }
    }
}