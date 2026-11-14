package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(vm: CalculatorViewModel = viewModel()) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Display(modifier = Modifier.weight(1f).fillMaxHeight(), vm = vm)
            Spacer(Modifier.width(8.dp))
            Keyboard(modifier = Modifier.weight(1f).fillMaxHeight(), vm = vm)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Display(modifier = Modifier.weight(1.2f).fillMaxWidth(), vm = vm)
            Spacer(Modifier.height(8.dp))
            Keyboard(modifier = Modifier.weight(2f).fillMaxWidth(), vm = vm)
        }
    }
}

@Composable
fun Display(modifier: Modifier, vm: CalculatorViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom, // Текст прижат к низу
        horizontalAlignment = Alignment.End // Текст выровнен по правому краю
    ) {
        Text( // Предыдущее выражение
            text = vm.previousExpression,
            color = Color.Gray,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            textAlign = TextAlign.End
        )
        Text( // Текущий оператор
            text = vm.currentOperator ?: "",
            color = Color(0xFF4FC3F7),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            textAlign = TextAlign.End
        )
        Text( // Число или ошибка
            text = vm.errorMessage ?: vm.currentNumber,
            color = if (vm.errorMessage != null) Color(0xFFFF5252) else Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            textAlign = TextAlign.End
        )
    }
    LaunchedEffect(vm.currentNumber, vm.errorMessage) {
        delay(50)
        scrollState.animateScrollTo(scrollState.maxValue)
    }
}

@Composable
fun Keyboard(modifier: Modifier, vm: CalculatorViewModel) {
    val rows = listOf(
        listOf("C", "(", ")", "⌫", "÷"),
        listOf("7", "8", "9", "×", "^"),
        listOf("4", "5", "6", "-", " "),
        listOf("1", "2", "3", "+", "="),
        listOf("0", ".", "00", "±", "%")
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f), // Заполняет вссе предоставлене пространство
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (btn in row) { // Создание кнопок
                    CalcButton(
                        text = btn,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { handlePress(btn, vm) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    val bg = when (text) {
        "=", "^", " " -> Color(0xFF4FC3F7)
        "÷", "×", "-", "+" -> Color(0xFF2A2A2A)
        "C", "⌫" -> Color(0xFF3A3A3A)
        else -> Color(0xFF1F1F1F)
    }
    val fg = when (text) {
        "=", "^", "!" -> Color.Black
        else -> Color.White
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

private fun handlePress(key: String, vm: CalculatorViewModel) {
    when (key) {
        "C" -> vm.onClear()
        "⌫" -> vm.onBackspace()
        "=" -> vm.onEquals()
        "+" -> vm.onOperator("+")
        "-" -> vm.onOperator("-")
        "×" -> vm.onOperator("×")
        "÷" -> vm.onOperator("÷")
        "^" -> vm.onOperator("^")
        "(" -> vm.onParen(open = true)
        ")" -> vm.onParen(open = false)
        "±" -> vm.onToggleSign()
        "%" -> vm.onPercent()
        else -> vm.onDigit(key)
    }
}