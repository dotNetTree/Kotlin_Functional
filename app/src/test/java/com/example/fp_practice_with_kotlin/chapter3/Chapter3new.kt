package com.example.fp_practice_with_kotlin.chapter3

import org.junit.Assert
import org.junit.Test
import com.example.fp_practice_with_kotlin.chapter3.FList.*
import java.nio.channels.FileLock

sealed class FList<out ITEM: Any> {
    data object Nil: FList<Nothing>()
    data class Cons<out ITEM: Any> @PublishedApi internal constructor(
        @PublishedApi internal val head: ITEM,
        @PublishedApi internal val tail: FList<ITEM>
    ): FList<ITEM>()
    companion object {
        inline operator fun <ITEM: Any> invoke(vararg items: ITEM): FList<ITEM>
            = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM: Any> invoke(): FList<ITEM> = Nil
    }
}

inline fun <ITEM: Any> FList<ITEM>.setHead(item: ITEM): FList<ITEM>
    = when (this) {
        is Cons -> Cons(item, tail)
        is Nil -> this
    }
inline fun <ITEM: Any> FList<ITEM>.addFirst(item: ITEM): FList<ITEM>
    = when (this) {
        is Cons -> Cons(item, this)
        is Nil -> Nil
    }
tailrec fun <ITEM: Any> FList<ITEM>.drop(n: Int = 1): FList<ITEM>           // drop first
    = if (n > 0 && this is Cons)  tail.drop(n - 1)  else this
tailrec fun <ITEM: Any> FList<ITEM>.dropWhile(block: (ITEM) -> Boolean): FList<ITEM>    // 해당 아이템이 나올때까지 앞을 끊는다. (해당 아이템은 포함된다.)
    = if (this is Cons && block(head)) tail.dropWhile(block) else this
tailrec fun <ITEM: Any> FList<ITEM>._dropWhileIndexed(index: Int, block: (Int, ITEM) -> Boolean): FList<ITEM>
    = if (this is Cons && block(index, head)) tail._dropWhileIndexed(index + 1, block) else this
inline fun <ITEM: Any> FList<ITEM>.dropWhileIndexed(noinline block: (Int, ITEM) -> Boolean): FList<ITEM>
    = _dropWhileIndexed(0, block)
fun <ITEM: Any> FList<ITEM>.append(list: FList<ITEM>): FList<ITEM>  // 재귀는 inline 넣으면 안됨
    = when (this) {
        is Cons -> Cons(head, tail.append(list))
        is Nil -> list
    }
inline fun <ITEM: Any> FList<ITEM>.copy(): FList<ITEM>
    = append(FList())
inline operator fun <ITEM: Any> FList<ITEM>.plus(list: FList<ITEM>): FList<ITEM>
    = append(list)
fun <ITEM: Any> FList<ITEM>.toList(): List<ITEM>
    = fold(listOf()) { acc, item -> acc + item }


fun <ITEM: Any> FList<ITEM>.dropLastOne(): FList<ITEM>  // 마지막 아이템만 자르기
    = when (this) {
        is Nil -> this
        is Cons -> if (tail is Nil) Nil else Cons(head, tail.dropLastOne())
    }
fun <ITEM: Any> FList<ITEM>.dropLast(n: Int = 1): FList<ITEM>
    = if (n > 0 && this is Cons) dropLastOne().dropLast(n - 1) else this
//    = when(this){
//        is Cons->if(n > 0) dropLastOne().dropLast(n - 1) else this
//        is Nil->this
//    }

fun <ITEM: Any, ACC: Any> FList<ITEM>.fold(base: ACC, block: (ACC, ITEM) -> ACC): ACC
    = when (this) {
        is Nil -> base
        is Cons -> tail.fold(block(base, head), block)
    }
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRight(base:ACC, origin:(ITEM, ACC)->ACC, block:(ACC)->ACC):ACC
    = when(this) {
        is Cons -> when(tail) {
            is Cons -> tail._foldRight(base, origin){acc ->block(origin(head, acc))}
            is Nil -> block(origin(head, base))
        }
        is Nil -> base
    }

fun <ITEM: Any, ACC: Any> FList<ITEM>.foldRight(base: ACC, origin: (ITEM, ACC) -> ACC): ACC
        = when (this) {
    is Nil -> base
    is Cons -> origin(head, tail.foldRight(base, origin))
}
val <ITEM: Any> FList<ITEM>.size: Int get() = foldRight(0) { it, acc -> acc + 1 }

fun <ITEM: Any, TO: Any> FList<ITEM>.map(block: (ITEM) -> TO): FList<TO>
    = foldRight(FList()) { it, acc -> Cons(block(it), acc) }

class Chapter3_4 {
    @Test
    fun main() {
        val list = FList(1, 2, 3)
        val nil = FList<Int>()
        val isTrue = FList<Int>(1,1,1).foldRight(0){it,acc->acc + it} == 3

        Assert.assertEquals(
            FList<Int>(1,2,3).foldRight(0){it,acc->acc + it},
            FList<Int>(1,2,3).fold(0){acc, it->acc + it}
        )

        Assert.assertEquals(list.dropLast().toList(), listOf(1,2))
        Assert.assertEquals(list.dropLast(2).toList(), listOf(1))
        Assert.assertEquals(nil.dropLast().toList(), listOf<Int>())
        Assert.assertEquals(nil.dropLast(2).toList(), listOf<Int>())

    }
}
