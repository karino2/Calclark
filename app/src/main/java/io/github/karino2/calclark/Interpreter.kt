package io.github.karino2.calclark

import com.google.common.collect.ImmutableMap
import net.starlark.java.annot.Param
import net.starlark.java.annot.StarlarkMethod
import net.starlark.java.eval.*
import net.starlark.java.syntax.FileOptions
import net.starlark.java.syntax.ParserInput
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

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
        name = "pow",
        parameters = [Param(name = "x"), Param(name = "y")],
        doc = "Returns x to the power of y (x**y)."
    )
    fun mathPow(x: Any, y: Any): StarlarkFloat {
        val xd = toDouble(x) ?: throw Starlark.errorf("Unsupported argument of pow x. %s", Starlark.type(x))
        val yd = toDouble(y) ?: throw Starlark.errorf("Unsupported argument of pow y. %s", Starlark.type(y))
        return StarlarkFloat.of(xd.pow(yd))
    }
}



class Interpreter {
    private val fileName = "<expr>"

    private fun makeEnv() : Map<String, Any> {
        val builder = ImmutableMap.builder<String, Any>()
        builder.put("pi", Math.PI)
        Starlark.addMethods(builder, MathFunctions(), StarlarkSemantics.DEFAULT)
        return builder.build()
    }

    private val module : Module by lazy {
        Module.withPredeclared(StarlarkSemantics.DEFAULT, makeEnv())
    }

    private val mutable: Mutability by lazy { Mutability.create(fileName) }

    private val evalThread: StarlarkThread by lazy { StarlarkThread(mutable, StarlarkSemantics.DEFAULT) }

    fun evalString(expr: String) : Any {
        val input = ParserInput.fromString(expr, fileName)
        return Starlark.eval(input, FileOptions.DEFAULT, module, evalThread)
    }
}