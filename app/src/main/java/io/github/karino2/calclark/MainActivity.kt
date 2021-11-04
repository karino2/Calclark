package io.github.karino2.calclark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import io.github.karino2.calclark.ui.theme.CalclarkTheme

data class Equation(val expression: String, val answer: String, val exception: String = "")


class MainActivity : ComponentActivity() {
    private val intp = Interpreter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalclarkTheme {
                Column(modifier = Modifier.fillMaxSize()) {

                    // history
                    var history by remember { mutableStateOf( emptyList<Equation>() ) }
                    // var history by remember { mutableStateOf( listOf(Equation("3+4", "7")) ) }

                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier
                        .verticalScroll(scrollState)) {
                        history.forEach {
                            Text( "${it.expression} = ${it.answer}")
                        }
                    }

                    var textState by remember { mutableStateOf("") }
                    TextField(
                        value = textState,
                        onValueChange = {textState = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.End),
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
                        })
                    )

                }
            }
        }
    }
}

