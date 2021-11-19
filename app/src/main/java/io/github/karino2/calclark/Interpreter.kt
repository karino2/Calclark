package io.github.karino2.calclark

import com.google.common.collect.ImmutableMap
import net.starlark.java.annot.Param
import net.starlark.java.annot.StarlarkMethod
import net.starlark.java.eval.*
import net.starlark.java.syntax.FileOptions
import net.starlark.java.syntax.ParserInput
import kotlin.math.*

class MathFunctions {

    fun toDouble(x: Any) : Double? {
        return when(x) {
            is StarlarkInt -> {
                x.toFiniteDouble()
            }
            is StarlarkFloat -> {
                x.toDouble()
            }
            else -> null
        }
    }

    private fun singleArgFloatFunc(x: Any, fname: String, f: (Double)->Double) : StarlarkFloat {
        return toDouble(x)?.let { StarlarkFloat.of(f(it)) } ?: throw Starlark.errorf("Unsupported argument of $fname. %s", Starlark.type(x))
    }

    private fun twoArgFloatFunc(x: Any, y:Any, fname: String, f: (Double, Double)->Double) : StarlarkFloat {
        val xd = toDouble(x) ?: throw Starlark.errorf("Unsupported argument of $fname x. %s", Starlark.type(x))
        val yd = toDouble(y) ?: throw Starlark.errorf("Unsupported argument of $fname y. %s", Starlark.type(y))
        return StarlarkFloat.of(f(xd, yd))
    }

    /*
    Trigonometric functions.
     */

    @StarlarkMethod(
        name = "acos",
        parameters = [Param(name = "x")],
        doc = "Returns the arc cosine (measured in radians) of x."
    )
    fun mathAcos(x: Any) = singleArgFloatFunc(x, "acos", ::acos)

    @StarlarkMethod(
        name = "acosh",
        parameters = [Param(name = "x")],
        doc = "Returns the inverse hyperbolic cosine of x."
    )
    fun mathAcosh(x: Any) = singleArgFloatFunc(x, "acosh", ::acosh)

    @StarlarkMethod(
        name = "asin",
        parameters = [Param(name = "x")],
        doc = "Returns the arc sine (measured in radians) of x."
    )
    fun mathAsin(x: Any) = singleArgFloatFunc(x, "asin", ::asin)

    @StarlarkMethod(
        name = "asinh",
        parameters = [Param(name = "x")],
        doc = "Returns the inverse hyperbolic sin of x."
    )
    fun mathAsinh(x: Any) = singleArgFloatFunc(x, "asinh", ::asinh)

    @StarlarkMethod(
        name = "atan",
        parameters = [Param(name = "x")],
        doc = "Returns the arc tangent (measured in radians) of x."
    )
    fun mathAtan(x: Any) = singleArgFloatFunc(x, "atan", ::atan)

    @StarlarkMethod(
        name = "atan2",
        parameters = [Param(name = "x"), Param(name = "y")],
        doc = "Returns the arc tangent (measured in radians) of y/x."
    )
    fun mathAtan2(x: Any, y:Any) = twoArgFloatFunc(x, y, "atan2", ::atan2)

    @StarlarkMethod(
        name = "atanh",
        parameters = [Param(name = "x")],
        doc = "Returns the inverse hyperbolic tangent of x."
    )
    fun mathAtanh(x: Any) = singleArgFloatFunc(x, "atanh", ::atanh)

    @StarlarkMethod(
        name = "cos",
        parameters = [Param(name = "x")],
        doc = "Returns the cosine (measured in radians) of x."
    )
    fun mathCos(x: Any) = singleArgFloatFunc(x, "cos", ::cos)

    @StarlarkMethod(
        name = "cosh",
        parameters = [Param(name = "x")],
        doc = "Returns the hyperbolic cosine of x."
    )
    fun mathCosh(x: Any) = singleArgFloatFunc(x, "cosh", ::cosh)

    @StarlarkMethod(
        name = "sin",
        parameters = [Param(name = "x")],
        doc = "Returns the sine (measured in radians) of x."
    )
    fun mathSin(x: Any) = singleArgFloatFunc(x, "sin", ::sin)

    @StarlarkMethod(
        name = "sinh",
        parameters = [Param(name = "x")],
        doc = "Returns the hyperbolic sin of x."
    )
    fun mathSinh(x: Any) = singleArgFloatFunc(x, "sinh", ::sinh)

    @StarlarkMethod(
        name = "tan",
        parameters = [Param(name = "x")],
        doc = "Returns the tangent (measured in radians) of x."
    )
    fun mathTan(x: Any) = singleArgFloatFunc(x, "tan", ::tan)

    @StarlarkMethod(
        name = "tanh",
        parameters = [Param(name = "x")],
        doc = "Returns the hyperbolic tangent of x."
    )
    fun mathTanh(x: Any) = singleArgFloatFunc(x, "tanh", ::tanh)

    /*
    log, exp
     */

    @StarlarkMethod(
        name = "log",
        parameters = [Param(name = "x")],
        doc = "Returns the ln of its argument."
    )
    fun mathLog(x: Any) = singleArgFloatFunc(x, "log", ::ln)

    @StarlarkMethod(
        name = "log10",
        parameters = [Param(name = "x")],
        doc = "Returns the log base 10 of its argument."
    )
    fun mathLog10(x: Any) = singleArgFloatFunc(x, "log10", ::log10)

    @StarlarkMethod(
        name = "log2",
        parameters = [Param(name = "x")],
        doc = "Returns the log base 2 of its argument."
    )
    fun mathLog2(x: Any) = singleArgFloatFunc(x, "log2", ::log2)

    @StarlarkMethod(
        name = "exp",
        parameters = [Param(name = "x")],
        doc = "Returns the exponential of x."
    )
    fun mathExp(x: Any) = singleArgFloatFunc(x, "exp", ::exp)

    @StarlarkMethod(
        name = "pow",
        parameters = [Param(name = "x"), Param(name = "y")],
        doc = "Returns x to the power of y (x**y)."
    )
    fun mathPow(x: Any, y: Any) = twoArgFloatFunc(x, y, "pow", f= {xd, yd-> xd.pow(yd) })
}



class Interpreter {
    private val fileName = "<expr>"

    private fun makeEnv() : Map<String, Any> {
        val builder = ImmutableMap.builder<String, Any>()
        builder.put("pi", Math.PI)
        builder.put("Out", outputs)
        Starlark.addMethods(builder, MathFunctions(), StarlarkSemantics.DEFAULT)
        return builder.build()
    }

    private val module : Module by lazy {
        Module.withPredeclared(StarlarkSemantics.DEFAULT, makeEnv())
    }

    private val mutable: Mutability by lazy { Mutability.create(fileName) }

    private val evalThread: StarlarkThread by lazy { StarlarkThread(mutable, StarlarkSemantics.DEFAULT) }

    val outputs by lazy { StarlarkList.newList<Any>(evalThread.mutability())!! }

    fun evalString(expr: String) : Any {
        val input = ParserInput.fromString(expr, fileName)
        return Starlark.eval(input, FileOptions.DEFAULT, module, evalThread)
    }
}