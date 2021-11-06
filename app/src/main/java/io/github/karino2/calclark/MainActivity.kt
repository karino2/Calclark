package io.github.karino2.calclark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.karino2.calclark.ui.theme.CalclarkTheme
import kotlinx.coroutines.launch

data class Equation(val expression: String, val answer: String, val exception: String = "") {
    val isException: Boolean
        get() = exception != ""
}


class MainActivity : ComponentActivity() {
    private val intp = Interpreter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalclarkTheme {
                Column(modifier = Modifier.fillMaxSize()) {

                    var history by remember { mutableStateOf( emptyList<Equation>() ) }
                    // var history by remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }

                    val scrollState = rememberScrollState()
                    val cscope = rememberCoroutineScope()

                    Column(modifier = Modifier
                        .weight(1.0f)
                        .verticalScroll(scrollState)) {
                        history.forEach {
                            EquationRow(it)
                        }
                    }
                    var textState by remember { mutableStateOf("") }
                    TextField(
                        value = textState,
                        placeholder= { Text("ex: (3**2)/4") },
                        onValueChange = {textState = it},
                        modifier = Modifier
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {

                            val resEx =
                                try {
                                    val res = intp.evalString(textState)
                                    Equation(textState, res.toString())
                                }catch(e: Exception) {
                                    Equation(textState, "", e.message ?: "")
                                }
                            history = history + listOf(resEx)
                            textState = ""
                            cscope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        })
                    )
                }
            }
        }
    }

}

@Composable
fun EquationRow(equation: Equation) {
    Column(modifier=Modifier.padding(0.dp, 2.dp)) {
        Card(modifier=Modifier.fillMaxWidth(), border= BorderStroke(2.dp, Color.Black)) {
            Text(equation.expression, fontSize = 20.sp, modifier=Modifier.padding(4.dp, 0.dp))
        }
        if (equation.isException) {
            Card(modifier=Modifier.fillMaxWidth(), border= BorderStroke(2.dp, Color.Black)) {
                Row(modifier=Modifier.height(IntrinsicSize.Min)) {
                    Text("exception", fontSize = 20.sp, color=Color.Red,  modifier = Modifier.padding(5.dp, 0.dp))
                    Divider(color = Color.Black, modifier = Modifier.fillMaxHeight().width(2.dp), thickness = 2.dp)
                    Text(equation.exception, fontSize = 20.sp,  color=Color.Red, modifier = Modifier.padding(5.dp, 0.dp))
                }
            }
        }
        else {
            Card(modifier=Modifier.fillMaxWidth(), border= BorderStroke(2.dp, Color.Black)) {
                Row(modifier=Modifier.height(IntrinsicSize.Min)) {
                    Text("=", fontSize = 20.sp, modifier = Modifier.padding(5.dp, 0.dp))
                    Divider(color = Color.Black, modifier = Modifier.fillMaxHeight().width(2.dp), thickness = 2.dp)
                    Text(equation.answer, fontSize = 20.sp, modifier = Modifier.padding(5.dp, 0.dp))
                }
            }
        }
    }
}


