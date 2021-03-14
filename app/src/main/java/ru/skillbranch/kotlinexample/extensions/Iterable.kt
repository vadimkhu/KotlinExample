package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    val it = listIterator(size)
    while (it.hasPrevious()) {
        if (predicate(it.previous())){
            return take(it.nextIndex())
        }
    }
    return emptyList()
}