package com.example.fp_practice_with_kotlin.chapter3

import com.example.fp_practice_with_kotlin.chapter3.FList.Cons
import com.example.fp_practice_with_kotlin.chapter3.FList.Nil
import com.example.fp_practice_with_kotlin.chapter4.FOption

sealed class FList<out ITEM:Any>{
    data object Nil:FList<Nothing>()
    data class Cons<out ITEM:Any>@PublishedApi internal constructor(
        @PublishedApi internal val head: ITEM,
        @PublishedApi internal val tail: FList<ITEM>):
        FList<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(vararg items:ITEM): FList<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM:Any> invoke(): FList<ITEM> = Nil
    }
}
inline val <ITEM: Any> FList<ITEM>.size:Int get() = _size(0)
inline val <ITEM: Any> FList<ITEM>.isEmpty: Boolean get() = _size(0) == 0
@PublishedApi internal tailrec fun <ITEM: Any> FList<ITEM>._size(acc: Int): Int =
    when(this) {
        is Cons -> tail._size(acc + 1)
        is Nil -> acc
    }
tailrec fun <ITEM: Any, ACC: Any> FList<ITEM>.fold(acc: ACC, block: (ACC, ITEM) -> ACC): ACC =
    when (this) {
        is Cons -> tail.fold(block(acc, head), block)
        is Nil -> acc
    }
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM>
        = fold(FList()){acc, it->Cons(it, acc)}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC
        = reverse().fold(base){ acc, it->block(it, acc)}
tailrec fun <ITEM:Any> FList<ITEM>.drop(n:Int = 1): FList<ITEM> =
    if (n > 0 && this is Cons) tail.drop(n - 1) else this
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER>
        = foldRight(FList()){ it, acc->Cons(block(it), acc)}
inline fun <ITEM:Any> FList<ITEM>.append(list: FList<ITEM> = FList()): FList<ITEM>
        = foldRight(list, ::Cons)
fun <ITEM:Any> FList<ITEM>.filter(block:(ITEM)->Boolean): FList<ITEM>
        = foldRight(FList()){it, acc->if(block(it)) Cons(it, acc) else acc}
