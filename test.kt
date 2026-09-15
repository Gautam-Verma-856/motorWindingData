fun main() {
    val a: Any = 2.0
    val b: Any = 2L
    val c: Any = "2"
    val d: Any = true
    val e: Any = "true"
    println(a.toString().toDoubleOrNull()?.toInt() ?: 0)
    println(b.toString().toDoubleOrNull()?.toInt() ?: 0)
    println(c.toString().toDoubleOrNull()?.toInt() ?: 0)
    println(d.toString().toBooleanStrictOrNull() ?: d.toString().toBoolean())
    println(e.toString().toBooleanStrictOrNull() ?: e.toString().toBoolean())
}
