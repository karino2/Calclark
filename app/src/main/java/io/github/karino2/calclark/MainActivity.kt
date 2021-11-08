package io.github.karino2.calclark

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
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

    private val clipMgr by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager}

    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private var history = mutableStateOf(emptyList<Equation>())
    // var history by remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Calclark(
                history,
                onEval = {text->
                    val resEx =
                        try {
                            val res = intp.evalString(text)
                            Equation(text, res.toString())
                        } catch (e: Exception) {
                            Equation(text, "", e.message ?: "")
                        }
                    history.value = history.value + listOf(resEx)
                },
                onCopy = {text->
                    clipMgr.setPrimaryClip(ClipData.newPlainText("Copied value", text))
                    showToast("copied: $text")
                }
            )
        }
    }
}

// var history by remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }
@Preview
@Composable
fun Preview() {
    var history = remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }
    Calclark(history, onEval={}, onCopy ={})
}

@Composable
fun Calclark(history: State<List<Equation>>, onEval: (String)->Unit, onCopy: (String)->Unit) {
    CalclarkTheme {
        Column(modifier = Modifier.fillMaxSize()) {

            val scrollState = rememberScrollState()
            val cscope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .verticalScroll(scrollState)
            ) {
                history.value.forEach {
                    EquationRow(it, onCopyText = { text ->
                        onCopy(text)
                    })
                }
            }
            var textState by remember { mutableStateOf("") }
            TextField(
                value = textState,
                placeholder = { Text("ex: (3**2)/4") },
                onValueChange = { textState = it },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    // ignore for empty case. (because often happens by accident).
                    if (textState == "")
                        return@KeyboardActions

                    onEval(textState)
                    textState = ""
                    cscope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                })
            )
        }
    }
}


@Composable
fun EquationRow(equation: Equation, onCopyText: (String)->Unit) {
    Column(modifier=Modifier.padding(0.dp, 2.dp)) {
        Card(modifier=Modifier.fillMaxWidth().clickable(onClick={ onCopyText(equation.expression) }), border= BorderStroke(2.dp, Color.Black)) {
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
                    Text(equation.answer, fontSize = 20.sp, modifier = Modifier.padding(5.dp, 0.dp).clickable(onClick={ onCopyText(equation.answer) }))
                }
            }
        }
    }
}


