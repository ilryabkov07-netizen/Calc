package com.example.calc
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView

    private var currentInput = ""
    private var expression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        // Цифры
        val digits = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6",
            R.id.btn7 to "7", R.id.btn8 to "8", R.id.btn9 to "9"
        )
        digits.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener { appendDigit(value) }
        }

        // Операторы
        findViewById<Button>(R.id.btnAdd).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { appendOperator("/") }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { appendOperator("%") }

        findViewById<Button>(R.id.btnDot).setOnClickListener { appendDot() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnDel).setOnClickListener { deleteLast() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
    }

    private fun appendDigit(d: String) {
        if (currentInput == "0") currentInput = d
        else currentInput += d
        updateResult()
    }

    private fun appendDot() {
        if (currentInput.isEmpty()) currentInput = "0."
        else if (!currentInput.contains(".")) currentInput += "."
        updateResult()
    }

    private fun appendOperator(op: String) {
        if (currentInput.isEmpty() && expression.isEmpty()) return
        if (currentInput.isNotEmpty()) {
            expression += currentInput + op
            currentInput = ""
        } else {
            expression = expression.dropLast(1) + op
        }
        tvExpression.text = expression
    }

    private fun deleteLast() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            updateResult()
        } else if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            tvExpression.text = expression
        }
    }

    private fun clearAll() {
        currentInput = ""
        expression = ""
        tvExpression.text = ""
        tvResult.text = "0"
    }

    private fun updateResult() {
        tvResult.text = if (currentInput.isEmpty()) "0" else currentInput
    }

    private fun calculateResult() {
        if (expression.isEmpty()) return
        val fullExpression = expression + currentInput
        try {
            val result = evaluate(fullExpression)
            tvExpression.text = fullExpression
            tvResult.text = formatResult(result)
            currentInput = result.toPlainString()
            expression = ""
        } catch (e: Exception) {
            tvResult.text = "Ошибка"
            clearAll()
        }
    }

    private fun formatResult(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return stripped.toPlainString()
    }

    private fun evaluate(expr: String): BigDecimal {
        val tokens = tokenize(expr)
        val firstPass = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            if (t == "*" || t == "/" || t == "%") {
                val left = firstPass.removeAt(firstPass.size - 1).toBigDecimal()
                val right = tokens[i + 1].toBigDecimal()
                val res = when (t) {
                    "*" -> left.multiply(right)
                    "/" -> {
                        if (right.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                        left.divide(right, MathContext(15, RoundingMode.HALF_UP))
                    }
                    else -> left.remainder(right)
                }
                firstPass.add(res.toPlainString())
                i += 2
            } else {
                firstPass.add(t)
                i++
            }
        }

        var result = firstPass[0].toBigDecimal()
        var j = 1
        while (j < firstPass.size) {
            val op = firstPass[j]
            val right = firstPass[j + 1].toBigDecimal()
            result = when (op) {
                "+" -> result.add(right)
                "-" -> result.subtract(right)
                else -> throw IllegalArgumentException("Unknown op $op")
            }
            j += 2
        }
        return result
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        for (ch in expr) {
            if (ch.isDigit() || ch == '.') {
                sb.append(ch)
            } else if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                if (sb.isNotEmpty()) {
                    tokens.add(sb.toString()); sb.clear()
                }
                tokens.add(ch.toString())
            }
        }
        if (sb.isNotEmpty()) tokens.add(sb.toString())
        return tokens
    }
}