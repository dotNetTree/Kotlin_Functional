package com.example.fp_practice_with_kotlin.chapter5

import com.example.fp_practice_with_kotlin.chapter3.FList
import com.example.fp_practice_with_kotlin.chapter4.FOption
import com.example.fp_practice_with_kotlin.chapter4.FOption.*
import com.example.fp_practice_with_kotlin.chapter5.FStream.*
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
            val tt = items[0]
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