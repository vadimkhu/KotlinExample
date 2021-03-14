package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    var list = this.toMutableList()
    var item = list.last()
    while (list.size > 0 && !predicate(item)) {
        list.removeLast()
        if (list.size > 0)
            item = list.last()
    }
    if (list.size > 0 && predicate(item))
        list.removeLast()
    return list.toList()
}