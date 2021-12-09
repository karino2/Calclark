package io.github.karino2.calclark

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

    override fun onSaveInstanceState(outState: Bundle) {
        val hists = history.value
        val expressions = hists.map { it.expression }.toTypedArray()
        val types = hists.map { it.isException }.toBooleanArray()
        val answerOrException = hists.map { if(it.isException) it.exception else it.answer }.toTypedArray()

        outState.putStringArray("key_exps", expressions)
        outState.putBooleanArray("key_types", types)
        outState.putStringArray("key_aoe", answerOrException)

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val expressions = savedInstanceState.getStringArray("key_exps")!!
        val types = savedInstanceState.getBooleanArray("key_types")!!
        val answerOrException = savedInstanceState.getStringArray("key_aoe")!!

        history.value = types.mapIndexed { idx, isexp ->
            val answer = if(isexp) "" else answerOrException[idx]
            val exception = if(isexp) answerOrException[idx] else ""
            Equation(expressions[idx], answer, exception)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Calclark(
                history,
                onEval = {text->
                    val resEx =
                        try {
                            val res = intp.evalString(text)
                            val resStr = res.toString()
                            intp.addOutput(res)
                            Equation(text, resStr)
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
    val history = remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }
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
                var outIdx = 0
                history.value.forEach {
                    EquationRow(it, outIdx, onCopyText = { text ->
                        onCopy(text)
                    })
                    outIdx = if(it.isException) outIdx else outIdx+1
                }
            }

            var isNumMode by remember { mutableStateOf(true) }

            Row(modifier = Modifier.fillMaxWidth().padding(5.dp, 0.dp), horizontalArrangement = Arrangement.Center) {
                RadioLabel("num", isNumMode, onClick = { isNumMode = true })
                Spacer(modifier = Modifier.size(5.dp))
                RadioLabel("text", !isNumMode,
                    onClick = {
                        isNumMode = false
                    })
            }
            var textState by remember { mutableStateOf("") }

            fun doEval() {
                onEval(textState)
                textState = ""
                cscope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }

            if(isNumMode) {
                Box(modifier=Modifier.background(Color(0xFFDDDDDD)).fillMaxWidth().padding(15.dp)) {
                    Text(textState, fontSize=20.sp)
                }

                fun appendSymbol(sym: String) {
                    textState += sym
                }

                Column(modifier=Modifier.weight(0.7f).background(Color.Gray)) {
                    val opcolor = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFC606), contentColor = Color.Black)
                    val special = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray, contentColor = Color.White)
                    Row(modifier=Modifier.weight(1.0f)) {
                        CalcButton("AC", { textState = "" }, special)
                        CalcButton("(", ::appendSymbol, special)
                        CalcButton(")", ::appendSymbol, special)
                        CalcButton("/", ::appendSymbol, opcolor)
                    }
                    Row(modifier=Modifier.weight(1.0f)) {
                        CalcButton("7", ::appendSymbol)
                        CalcButton("8", ::appendSymbol)
                        CalcButton("9", ::appendSymbol)
                        CalcButton("*", ::appendSymbol, opcolor)
                    }
                    Row(modifier=Modifier.weight(1.0f)) {
                        CalcButton("4", ::appendSymbol)
                        CalcButton("5", ::appendSymbol)
                        CalcButton("6", ::appendSymbol)
                        CalcButton("-", ::appendSymbol, opcolor)
                    }
                    Row(modifier=Modifier.weight(1.0f)) {
                        CalcButton("1", ::appendSymbol)
                        CalcButton("2", ::appendSymbol)
                        CalcButton("3", ::appendSymbol)
                        CalcButton("+", ::appendSymbol, opcolor)
                    }
                    Row(modifier=Modifier.weight(1.0f)) {
                        CalcButton("0", ::appendSymbol)
                        CalcButton(".", ::appendSymbol)
                        CalcButton("BS", { textState = textState.substring(0, (textState.length -1).coerceAtLeast(0) )}, special)
                        CalcButton("|>", { doEval() }, ButtonDefaults.buttonColors(backgroundColor = Color(0xFF21EEC7), contentColor = Color.White))
                    }
                }
            } else {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = textState,
                    placeholder = { Text("ex: (3**2)/4") },
                    onValueChange = { textState = it },
                    textStyle= TextStyle(fontSize=20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        // ignore for empty case. (because often happens by accident).
                        if (textState == "")
                            return@KeyboardActions

                        doEval()
                    })
                )
                LaunchedEffect(true) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun RowScope.CalcButton(text:String, onClick: (String)->Unit,
                        buttonColors : ButtonColors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White)) {
    Button(modifier=Modifier.weight(1.0f).fillMaxHeight(), colors=buttonColors, onClick = { onClick(text) }) {
        Text(text, fontSize=25.sp)
    }
}

@Composable
fun RowScope.RadioLabel(text: String, checked: Boolean, onClick: ()->Unit) {
    val bgcolor = if(checked) MaterialTheme.colors.primary else Color.LightGray
    Box(modifier=
    Modifier
        .weight(1.0f)
        .selectable(selected = checked, onClick = onClick)
        .background(color = bgcolor, shape = CircleShape)) {
        Text(text, fontSize=23.sp, modifier=Modifier.align(Alignment.Center), color= contentColorFor(backgroundColor = bgcolor))
    }

}


@Composable
fun EquationRow(equation: Equation, outIdx: Int, onCopyText: (String)->Unit) {
    val textPadding = 10.dp
    Card(elevation = 8.dp, modifier=Modifier.padding(4.dp)) {
        Column {
            Card(modifier= Modifier
                .fillMaxWidth()
                .clickable(onClick = { onCopyText(equation.expression) })) {
                Text(equation.expression, fontSize = 20.sp, modifier=Modifier.background(Color(0xFFE0E0E0)).padding(textPadding, 0.dp))
            }
            if (equation.isException) {
                Card(modifier=Modifier.fillMaxWidth(), border= BorderStroke(2.dp, Color.Black)) {
                    Row(modifier=Modifier.height(IntrinsicSize.Min)) {
                        Text("exception", fontSize = 20.sp, color=Color.Red,  modifier = Modifier.padding(textPadding, 0.dp))
                        Divider(color = Color.Black, modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp), thickness = 2.dp)
                        Text(equation.exception, fontSize = 20.sp,  color=Color.Red, modifier = Modifier.padding(textPadding, 0.dp))
                    }
                }
            }
            else {
                Card(modifier=Modifier.fillMaxWidth()) {
                    Row(modifier=Modifier.height(IntrinsicSize.Min)) {
                        Text("Out[$outIdx] =", fontSize = 20.sp, modifier = Modifier.padding(textPadding, 0.dp))
                        Text(equation.answer, fontSize = 20.sp, modifier = Modifier
                            .padding(5.dp, 0.dp)
                            .clickable(onClick = { onCopyText(equation.answer) }))
                    }
                }
            }

        }
    }
}


