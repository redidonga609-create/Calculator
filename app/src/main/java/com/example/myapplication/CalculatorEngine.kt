package com.example.myapplication

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function

object CalculatorEngine {

    class CalcException(msg: String) : Exception(msg)

    fun evaluate(expression: String): Double {
        return try {
            val normalized = expression
                .replace("×", "*")
                .replace("÷", "/")

            val result = ExpressionBuilder(normalized)
                .build()
                .evaluate()

            if (result.isInfinite() || result.isNaN()) {
                throw CalcException("Результат не определён")
            }
            result
        } catch (e: CalcException) {
            throw e
        } catch (e: Exception) {
            throw CalcException("Некорректное выражение")
        }
    }



}