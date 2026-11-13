package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat

class CalculatorViewModel : ViewModel() {

    var previousExpression by mutableStateOf("")
        private set
    var currentOperator by mutableStateOf<String?>(null)
        private set
    var currentNumber by mutableStateOf("0")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val rawExpression = StringBuilder()
    private var justEvaluated = false
    private val fmt = DecimalFormat("#.##########")

    fun onDigit(d: String) {
        errorMessage = null
        if (justEvaluated) {
            rawExpression.clear()
            previousExpression = ""
            currentOperator = null
            justEvaluated = false
        }
        currentNumber = if (currentNumber == "0" && d != ".") d
        else if (d == "." && currentNumber.contains('.')) currentNumber
        else currentNumber + d
    }

    fun onOperator(op: String) {
        errorMessage = null
        val symbol = when (op) {
            "×" -> "*"
            "÷" -> "/"
            else -> op
        }
        if (justEvaluated) {
            rawExpression.clear()
            justEvaluated = false
        }
        rawExpression.append(currentNumber).append(symbol)
        previousExpression = formatRaw(rawExpression.toString())
        currentOperator = op
        currentNumber = "0"
        justEvaluated = false
    }

    fun onFactorial() {
        errorMessage = null
        try {
            val v = currentNumber.toDouble()
            val r = CalculatorEngine.evaluate("$v!")
            currentNumber = formatResult(r)
            if (rawExpression.isNotEmpty() && currentOperator != null) {
                val s = rawExpression.toString()
                val lastIdx = maxOf(s.lastIndexOf('+'), s.lastIndexOf('-'), s.lastIndexOf('*'), s.lastIndexOf('/'))
                val head = if (lastIdx >= 0) s.substring(0, lastIdx + 1) else ""
                rawExpression.clear().append(head).append(currentNumber.toDouble()).append("!")
                previousExpression = formatRaw(rawExpression.toString()) + " $currentOperator "
            } else {
                rawExpression.clear().append(currentNumber.toDouble()).append("!")
                previousExpression = formatRaw(rawExpression.toString())
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Ошибка"
        }
    }

    fun onBackspace() {
        errorMessage = null
        if (justEvaluated) return
        if (currentNumber.length > 1) currentNumber = currentNumber.dropLast(1)
        else currentNumber = "0"
    }

    fun onClear() {
        rawExpression.clear()
        previousExpression = ""
        currentOperator = null
        currentNumber = "0"
        errorMessage = null
        justEvaluated = false
    }

    fun onEquals() {
        errorMessage = null

        // ИСПРАВЛЕНИЕ: если результат уже вычислен, просто выходим
        if (justEvaluated) {
            return
        }

        try {
            val raw = rawExpression.toString()
            val expr = if (raw.endsWith(")")) {
                raw
            } else {
                raw + currentNumber
            }

            val r = CalculatorEngine.evaluate(expr)
            val formatted = formatResult(r)
            previousExpression = "$expr ="
            currentOperator = null
            currentNumber = formatted
            rawExpression.clear().append(formatted)
            justEvaluated = true
        } catch (e: Exception) {
            errorMessage = e.message ?: "Ошибка вычисления"
        }
    }

    fun onParen(open: Boolean) {
        errorMessage = null
        if (open) {
            rawExpression.append("(")
            previousExpression = formatRaw(rawExpression.toString())
            currentNumber = "0"
        } else {
            rawExpression.append(currentNumber).append(")")
            previousExpression = formatRaw(rawExpression.toString())
            currentNumber = "0"
        }
    }

    // === ВОТ ЭТИ ДВЕ ФУНКЦИИ БЫЛИ ОТСУТСТВОВАЛИ ===
    fun onToggleSign() {
        errorMessage = null
        if (currentNumber == "0") return
        currentNumber = if (currentNumber.startsWith("-")) {
            currentNumber.drop(1)
        } else {
            "-" + currentNumber
        }
    }

    fun onPercent() {
        errorMessage = null
        val v = currentNumber.toDoubleOrNull() ?: return
        currentNumber = formatResult(v / 100.0)
    }
    // ==============================================

    private fun formatResult(v: Double): String {
        if (v.isInfinite() || v.isNaN()) throw CalculatorEngine.CalcException("Не определено")
        return if (v == Math.floor(v) && !v.isInfinite() && Math.abs(v) < 1e15) {
            v.toLong().toString()
        } else {
            fmt.format(v)
        }
    }

    private fun formatRaw(s: String): String = s.replace("*", "×").replace("/", "÷")
}