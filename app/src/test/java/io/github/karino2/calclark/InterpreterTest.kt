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

    /*
        sum tests
     */
    @Test
    fun sum_empty() {
        val res = intp.evalString("sum([])")
        assertEquals(StarlarkInt.of(0), res)
    }

    @Test
    fun sum_emptyWithStart() {
        val res = intp.evalString("sum([], 10)")
        assertEquals(StarlarkInt.of(10), res)
    }

    @Test
    fun sum_emptyWithStartNamed() {
        val res = intp.evalString("sum([], start=10)")
        assertEquals(StarlarkInt.of(10), res)
    }

    @Test
    fun sum_intList() {
        val res = intp.evalString("sum([1, 2, 3])")
        assertEquals(StarlarkInt.of(6), res)
    }

    @Test
    fun sum_intListWithIntStart() {
        val res = intp.evalString("sum([1, 2, 3], start=10)")
        assertEquals(StarlarkInt.of(16), res)
    }

    @Test
    fun sum_intListWithFloatStart() {
        val res = intp.evalString("sum([1, 2, 3], start=3.5)")
        assertEquals(StarlarkFloat.of(9.5), res)
    }

    @Test
    fun sum_floatList() {
        val res = intp.evalString("sum([1.1, 2.1, 3.1])")
        assertEquals(6.3, (res as StarlarkFloat).toDouble(), 0.0001)
    }

    @Test
    fun sum_floatListWithIntStart() {
        val res = intp.evalString("sum([1.1, 2.1, 3.1], start=10)")
        assertEquals(16.3, (res as StarlarkFloat).toDouble(), 0.0001)
    }

    @Test
    fun sum_floatListWithFloatStart() {
        val res = intp.evalString("sum([1.1, 2.1, 3.1], start=10.1)")
        assertEquals(16.4, (res as StarlarkFloat).toDouble(), 0.0001)
    }

    @Test
    fun sum_mixedList() {
        val res = intp.evalString("sum([1, 2.1, 3])")
        assertEquals(6.1, (res as StarlarkFloat).toDouble(), 0.0001)
    }

    @Test
    fun sum_mixedList2() {
        val res = intp.evalString("sum([1.1, 2, 3])")
        assertEquals(6.1, (res as StarlarkFloat).toDouble(), 0.0001)
    }
}