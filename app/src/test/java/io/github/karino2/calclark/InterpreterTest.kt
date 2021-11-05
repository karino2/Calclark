package io.github.karino2.calclark

import net.starlark.java.eval.StarlarkFloat
import net.starlark.java.eval.StarlarkInt
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class InterpreterTest {
    private val intp = Interpreter()

    @Test
    fun addition_isCorrect() {
        val res = intp.evalString("3+4")
        assertEquals(StarlarkInt.of(7), res)
    }

    @Test
    fun power_isCorrect() {
        val res = intp.evalString("3**2")
        assertEquals(StarlarkFloat.of(9.0), res)
    }


    @Test
    fun power_precedenceIsCorrect() {
        val res = intp.evalString("-3**2")
        assertEquals(StarlarkFloat.of(-9.0), res)
    }
}