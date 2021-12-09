# Calclark

Calclark is a [Starlark](https://github.com/bazelbuild/starlark)(Python dialect used in Basel) fork for calculator app on Android.

Use repl as a calculator for complex calculations.

## The difference between Starlark

There are a few differences from Starlark.

- Support `**` as a power operator
- `_`, `__` as a prev result and before prev result (like Jupyter)
- Array `Out[0]`, `Out[1]`, ... contains previous results
- Some math-related built-in functions

### Built-in functions

Same as evaluating the following code in python.

```
from math import *
```

Note that all functions are in the top-level namespace.

Additionally, there is the `sum` function.

## Open source software and library

Calclark uses the following software.

- Sarlark (fork from tag 6.0.0-pre.20211019.1)
- Guava