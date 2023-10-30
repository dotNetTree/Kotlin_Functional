package com.example.fp_practice_with_kotlin.chapter5

import com.example.fp_practice_with_kotlin.chapter3.dropLast
import com.example.fp_practice_with_kotlin.chapter3.toList
import com.example.fp_practice_with_kotlin.chapter4.FOption
import com.example.fp_practice_with_kotlin.chapter4.FOption.*
import com.example.fp_practice_with_kotlin.chapter5.FStream.*
import org.junit.Assert
import org.junit.Test

sealed class FStream<out VALUE: Any> {
    data class Cons<out VALUE: Any>internal constructor(
        val head: () -> VALUE,
        val tail: () -> FStream<VALUE>
    ): FStream<VALUE>()
    data object Empty: FStream<Nothing>()
    companion object{
        operator fun <VALUE: Any> invoke(
            head: () -> VALUE,
            tail: () -> FStream<VALUE>
        ): FStream<VALUE> = FStream.Cons(head, tail)
//        operator fun <VALUE: Any> invoke(
//            head: () -> VALUE,
//            tail: () -> FStream<VALUE>
//        ): FStream<VALUE> {
//            val _head: VALUE by lazy(head)
//            val _tail: FStream<VALUE> by lazy(tail)
//            return Cons({ _head }, { _tail })
//        }
//        inline operator fun <ITEM: Any> invoke(vararg items: ITEM): FList<ITEM>
        operator fun <VALUE: Any> invoke(
            vararg items: VALUE
        ): FStream<VALUE> {
            return if (items.isEmpty()) Empty
            else FStream(
                { items[0] },
                { FStream(items = items.sliceArray(1 until items.size)) }
            )
        }

        inline operator fun <VALUE: Any> invoke(): FStream<VALUE> = Empty
    }
}

val <VALUE: Any> FStream<VALUE>.headOption: FOption<VALUE> get() =
    when (this) {
        is Empty -> None
        is Cons -> Some(head())
    }

tailrec fun <ITEM:Any, ACC:Any> FStream<ITEM>._foldRight(base: () -> ACC, origin:(ITEM, () -> ACC) -> ACC, block: (() -> ACC) -> ACC): ACC
       = when (this) {
           is Cons -> when(tail()) {
               is Cons -> tail()._foldRight(base, origin) { acc -> block({ origin(head(), acc) }) }
               is Empty -> block({ origin(head(), base) })
           }
            is Empty -> base()
       }
fun <ITEM: Any, ACC: Any> FStream<ITEM>.foldRight(base: () -> ACC, origin: (ITEM, () -> ACC) -> ACC): ACC
    = _foldRight(base, origin) { it() }

fun <VALUE: Any, ACCU: Any> FStream<VALUE>.fold(base: () -> ACCU, block: (() -> ACCU, VALUE) -> ACCU): ACCU
        = when (this) {
            is Cons -> tail().fold({ block(base, head()) }, block)
            is Empty -> base()
        }
// ===================================
// ✏️[연습문제 5.1]
fun <VALUE: Any> FStream<VALUE>.toList(): List<VALUE>
    = fold({ listOf() }) { acc, it -> acc() + it }
class Chapter5_1 {
    @Test
    fun main() {
        val stream: FStream<Int> = FStream<Int>(1, 2, 3, 4)
        print(stream.foldRight({ 0 }) { it, acc -> acc() + it })
        Assert.assertEquals(stream.toList(), listOf(1, 2, 3, 4))
    }

}