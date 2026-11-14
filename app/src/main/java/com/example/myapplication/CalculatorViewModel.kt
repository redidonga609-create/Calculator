package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.floor

class CalculatorViewModel : ViewModel() {

    var expression by mutableStateOf("0") // Выражение
        private set

    var previousExpression by mutableStateOf("") // Для отображения
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val fmt = DecimalFormat("#.##########")



    // Текущее число
    val currentNumber: String
        get() {
            if (expression == "0") return "0"
            val lastOpIndex = expression.indexOfLast { it in "+×÷^(" }
            return expression.substring(lastOpIndex + 1).ifEmpty { "0" }
        }

    // Текущий оператор
    val currentOperator: String?
        get() {
            val lastChar = expression.lastOrNull()
            return if (lastChar != null && lastChar in "+-×÷^") {
                lastChar.toString()
            } else {
                null
            }
        }

    fun onDigit(d: String) {
        errorMessage = null
        // Если на экране только "0" или только что был результат, заменяем его
        if (expression == "0" || previousExpression.endsWith("=")) {
            expression = if (d == ".") "0." else d
        } else {
            expression += d
        }
        previousExpression = expression
    }

    fun onOperator(op: String) {
        errorMessage = null
        val lastChar = expression.lastOrNull()
        if (lastChar != null && lastChar in "+-×÷^") {
            expression = expression.dropLast(1) + op
        } else {
            expression += op
        }
        previousExpression = expression
    }


    fun onEquals() {
        errorMessage = null
        val cleanExpr = expression.trimEnd('+', '-', '×', '÷', '^')

        if (cleanExpr.isEmpty() || cleanExpr == "0") return

        try {
            val result = CalculatorEngine.evaluate(cleanExpr)
            val formatted = formatResult(result)

            previousExpression = "$cleanExpr ="
            expression = formatted // Результат становится новым выражением
        } catch (e: Exception) {
            errorMessage = e.message ?: "Ошибка вычисления"
        }
    }

    fun onBackspace() {
        errorMessage = null
        if (expression.endsWith("=") || expression == "0") {
            expression = "0"
        } else {
            expression = expression.dropLast(1).ifEmpty { "0" }
        }
        previousExpression = expression
    }

    fun onClear() {
        expression = "0"
        previousExpression = ""
        errorMessage = null
    }

    fun onParen(open: Boolean) {
        errorMessage = null
        if (open) {
            expression = if (expression == "0") "(" else expression + "("
        } else {
            // Добавляем закрывающую скобку только если выражение не пустое и не заканчивается на оператор
            val lastChar = expression.lastOrNull()
            if (lastChar != null && lastChar !in "+-×÷^(") {
                expression += ")"
            }
        }
        previousExpression = expression
    }

    fun onToggleSign() {
        errorMessage = null
        val num = currentNumber
        if (num == "0") return

        // Находим начало текущего числа в выражении
        val lastOpIndex = expression.indexOfLast { it in "+-×÷^(" }
        val startIdx = lastOpIndex + 1

        val currentPart = expression.substring(startIdx)
        val newPart = if (currentPart.startsWith("-")) currentPart.drop(1) else "-$currentPart"

        expression = expression.substring(0, startIdx) + newPart
        previousExpression = expression
    }

    fun onPercent() {
        errorMessage = null
        val v = currentNumber.toDoubleOrNull() ?: return
        val result = formatResult(v / 100.0)

        val lastOpIndex = expression.indexOfLast { it in "+-×÷^(" }
        val startIdx = lastOpIndex + 1
        expression = expression.substring(0, startIdx) + result
        previousExpression = expression
    }

    private fun formatResult(v: Double): String {
        if (v.isInfinite() || v.isNaN()) throw CalculatorEngine.CalcException("Не определено")
        return if (v == floor(v) && !v.isInfinite() && abs(v) < 1e15) {
            v.toLong().toString()
        } else {
            fmt.format(v)
        }
    }
}