package com.example.fp_practice_with_kotlin.chapter4

import org.junit.Assert
import org.junit.Test
import java.lang.ArithmeticException

class Chapter4 {
    @Test
    fun main() {
        println("이제 시작 !!!")
    }
}

// list4.1 예외 던지고 받기
fun failingFn(i: Int): Int {
    val y: Int = throw Exception("boom")
    return try {
        val x = 42 + 5
        x + y
    } catch (e: Exception) {
        43 // 도달할 수 없는 코드여서 43이 반환되지 않음
    }
}
fun failingFn2(i: Int): Int {
    return try {
        val x = 42 + 5
        x + (throw Exception("boom")) as Int
    } catch (e: Exception) {
        43 // 예외가 잡히고 43이 반환됨
    }
}
class Chapter4_list_1 {
    @Test fun test1() { println(failingFn(12)) }
    @Test fun test2() { println(failingFn2(12)) }
}

fun mean(xs: List<Double>): Double =
    if (xs.isEmpty())
        throw ArithmeticException("mean of empty list!")
    else
        xs.sum() / xs.size
