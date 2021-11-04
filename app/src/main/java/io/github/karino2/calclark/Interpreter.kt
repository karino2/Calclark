package io.github.karino2.calclark

import com.google.common.collect.ImmutableMap
import net.starlark.java.eval.*
import net.starlark.java.syntax.FileOptions
import net.starlark.java.syntax.ParserInput

class Interpreter {
    private val fileName = "<expr>"

    private fun makeEnv() : Map<String, Object> = ImmutableMap.of()

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