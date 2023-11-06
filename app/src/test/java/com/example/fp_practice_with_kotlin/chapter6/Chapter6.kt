package com.example.fp_practice_with_kotlin.chapter6

import com.example.fp_practice_with_kotlin.chapter3.FList
import com.example.fp_practice_with_kotlin.chapter3.append
import org.junit.Assert
import org.junit.Test

/* RandomNumberGenerator */
interface RNG {
    fun nextInt(): Pair<Int, RNG>
}
data class SimpleRNG(val seed: Long): RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }

}

val RNG.nonNegativeInt: Pair<Int, RNG> get()  {
    val (n, nextRNG) = this.nextInt()
    return (if (n < 0) -(n + 1) else n) to nextRNG
}
val RNG.double: Pair<Double, RNG> get() {
    val (n, nextRNG) = nonNegativeInt
    return n / (Int.MAX_VALUE.toDouble() + 1) to nextRNG
}
val RNG.intDouble: Pair<Pair<Int, Double>, RNG> get() {
    val (i, nextRNG1) = nonNegativeInt
    val (d, nextRNG2) = nextRNG1.double
    return (i to d) to nextRNG2
}
val RNG.doubleInt: Pair<Pair<Double, Int>, RNG> get() {
    val (gen, nextRNG) = intDouble
    return ((gen.second to gen.first) to nextRNG)
}

//fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
//    val (generated1, rng1) = double(rng)
//
//}

fun RNG.ints(count: Int): Pair<FList<Int>, RNG> =
    if (count > 0) {
        val (i, nextRNG1) = nextInt()
        val (list, nextRNG2) = nextRNG1.ints(count - 1)
        FList(i).append(list) to nextRNG2
    } else {
        FList<Int>() to this
    }

//typealias LazyRand<T> = (RNG) -> Pair<T, RNG>
//val RNG.lazyRand: LazyRand<Int> get() = { nextInt() }
//fun <A: Any> RNG.unit(a: A): LazyRand<A> = { a to this }
//fun <A: Any, B: Any> LazyRand<A>.map(block: (A) -> B): LazyRand<B> = {
//    val (a, nextRNG) = this(it)
//    block(a) to nextRNG
//}
//val RNG.doubleR: LazyRand<Double> get() = { it ->
//    ({ it: RNG -> it.nonNegativeInt }.map { it / (Int.MAX_VALUE.toDouble() + 1) })(it)
//}
////fun <A: Any, B: Any, C: Any> LazyRand<A>.map2(lr: LazyRand<B>, block: (A, B) -> C): LazyRand<C> = {
////    val (a, nextRNG) = this(it)
////    val (b, nextRNG2) = lr()
////    block(a) to nextRNG
////}
//class Chapter6 {
//    @Test
//    fun main() {
//        val rng = SimpleRNG(1)
//        println(rng.nonNegativeInt)
//        println(rng.nonNegativeInt) // random인데 상태가 저장된다!
//        println(rng.nonNegativeInt.second.nonNegativeInt)   // rng의 next rng의 생성값도 똑같다!
//        println(rng.nonNegativeInt.second.nonNegativeInt)
//        val ints = rng.ints(2)
//        println(ints.first)
//        println(ints.second.ints(5).first)
//        println(rng.ints(6).first)  // seed가 같으면 계속 같은 값이 유지된다.
//
//        println(rng.double)
//        println(rng.doubleR(rng))
////        println(rng.lazyRand.map { it.toDouble() }())
//
//        Assert.assertEquals(listOf(1, 2, 3, 4), listOf(1, 2, 3, 4))
//    }
//}

//typealias LazyRand<T> = () -> Pair<T, RNG>
//val RNG.lazyRand: LazyRand<Int> get() = { nextInt() }
//fun <A: Any> RNG.unit(a: A): LazyRand<A> = { a to this }
//fun <A: Any, B: Any> LazyRand<A>.map(block: (A) -> B): LazyRand<B> = {
//    val (a, nextRNG) = this()
//    block(a) to nextRNG
//}
//val RNG.doubleR: LazyRand<Double> get() = {
//    { nonNegativeInt }.map { it / (Int.MAX_VALUE.toDouble() + 1) }()
//}

typealias Rand<T> = (RNG) -> Pair<T, RNG>
val intR: Rand<Int> = { it.nextInt() }
fun <T> unit(t: T): Rand<T> = { t to it }
fun <A: Any, B: Any> Rand<A>.map(block: (A) -> B): Rand<B> = { rng ->
    val (a, nextRNG) = this(rng)
    block(a)to nextRNG
}
fun nonNegativeEven(): Rand<Int> = { rng: RNG -> rng.nonNegativeInt }.map { it - (it % 2) }
fun doubleR(): Rand<Double> = { rng: RNG -> rng.nonNegativeInt }.map { it / (Int.MAX_VALUE.toDouble() + 1) }
fun <A: Any, B: Any, C: Any> Rand<A>.map2(rb: Rand<B>, combine: (A, B) -> C): Rand<C> = { rng ->
    val (a, rng1) = this(rng)  // 첫 번째 Rand를 평가하여 결과와 새로운 RNG 상태를 얻음
    val (b, rng2) = rb(rng1)   // rng1 상태를 사용하여 두 번째 Rand를 평가
    val c = combine(a, b)      // 두 결과를 combine 함수를 사용해 조합
    Pair(c, rng2)      // 새로운 값을 최종 RNG 상태와 함께 반환
}
class Chapter6 {
    @Test
    fun main() {
        val rng = SimpleRNG(1)

        println(rng.nonNegativeInt)
        println(rng.nonNegativeInt) // random인데 상태가 저장된다!
        println(rng.nonNegativeInt.second.nonNegativeInt)   // rng의 next rng의 생성값도 똑같다!
        println(rng.nonNegativeInt.second.nonNegativeInt)
        val ints = rng.ints(2)
        println(ints.first)
        println(ints.second.ints(5).first)
        println(rng.ints(6).first)  // seed가 같으면 계속 같은 값이 유지된다.

        println(rng.double)
        println(doubleR()(rng))
        Assert.assertEquals(rng.double, doubleR()(rng))

        Assert.assertEquals(listOf(1, 2, 3, 4), listOf(1, 2, 3, 4))
    }
}