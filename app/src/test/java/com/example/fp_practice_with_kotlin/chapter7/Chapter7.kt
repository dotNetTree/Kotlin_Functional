import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

fun <T> List<T>.splitAt(index: Int): Pair<List<T>, List<T>> =
    this.subList(0, index) to this.subList(index, this.size)

class TestPar<VALUE: Any> private constructor(val unit: () -> VALUE) {
    companion object{
        operator fun <VALUE: Any> invoke(unit: () -> VALUE): TestPar<VALUE> {
            return TestPar(unit)
        }

    }
}

// 연습문제 7.2
inline fun <VALUE: Any> TestPar<VALUE>.fork(): TestPar<VALUE> {
    return this
}
inline fun <VALUE: Any> TestPar<VALUE>.lazyUnit(noinline block: () -> VALUE): TestPar<VALUE> {
    return TestPar(block)
}
inline fun <VALUE: Any> TestPar<VALUE>.run(): VALUE = unit()

// 연습문제 7.1
inline fun <LEFT: Any, RIGHT: Any, TO: Any> TestPar<LEFT>.combine(
    other: TestPar<RIGHT>,
    noinline block: (LEFT, RIGHT) -> TO
): TestPar<TO>
    = TestPar { block(this.unit(), other.unit()) }


// 리스트7.3 새 데이터 타입을 사용해 병렬성 통합하기
fun sum(ints: List<Int>): Int =
    if (ints.size <= 1)
        ints.firstOrNull() ?: 0
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        val sumL: TestPar<Int> = TestPar { sum(l) }
        val sumR: TestPar<Int> = TestPar { sum(r) }
        TestPar { sum(l) }.combine(TestPar { sum(r) }) { l, r -> l + r }.unit()
    }

//==================================================

typealias Par<VALUE> = (ExecutorService) -> Future<VALUE>
fun <VALUE: Any, OTHER: Any, TO: Any> Par<VALUE>.combine(
    other: Par<OTHER>,
    block: (VALUE, OTHER) -> TO
): Par<TO> = { es: ExecutorService ->
    val lf: Future<VALUE> = this(es)
    val rf: Future<OTHER> = other(es)
    UnitFuture(block(lf.get(), rf.get()))
}

fun <VALUE: Any> Par<VALUE>.run(es: ExecutorService): VALUE = this(es).get()

object Pars {

    fun <VALUE: Any> unit(v: VALUE): Par<VALUE> = { es: ExecutorService -> UnitFuture(v) }
    fun <VALUE: Any> fork(block: () -> Par<VALUE>): Par<VALUE> = { es: ExecutorService ->
        es.submit(Callable<VALUE> { block()(es).get() })
    }
    fun <VALUE: Any> lazyUnit(block: () -> VALUE): Par<VALUE> = { es: ExecutorService ->
        fork { unit(block()) }(es)
    }
    fun <VALUE: Any, TO: Any> asyncF(block: (VALUE) -> TO): (VALUE) -> Par<TO> = {
        lazyUnit { block(it) }
    }
}

data class UnitFuture<VALUE>(val v: VALUE): Future<VALUE> {
    override fun get(): VALUE = v
    override fun get(p0: Long, p1: TimeUnit?): VALUE = v
    override fun cancel(p0: Boolean): Boolean = false
    override fun isDone(): Boolean = true
    override fun isCancelled(): Boolean = false
}

// 연습 문제 7.3
data class TimeMap2Future<VALUE: Any, OTHER: Any, TO: Any>(
    val pa: Future<VALUE>,
    val pb: Future<OTHER>,
    val block: (VALUE, OTHER) -> TO
): Future<TO> {
    override fun cancel(p0: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCancelled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDone(): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(): TO {
        TODO("Not yet implemented")
    }

    override fun get(to: Long, tu: TimeUnit?): TO {
        val timeoutMillis = TimeUnit.MICROSECONDS.convert(to, tu)

        val start = System.currentTimeMillis()
        val a = pa.get(to, tu)
        val duration = System.currentTimeMillis() - start

        val remainder = timeoutMillis - duration
        val b = pb.get(remainder, TimeUnit.MICROSECONDS)
        return block(a, b)
    }
}

fun sum2(es: ExecutorService, ints: List<Int>): Int =
    if (ints.size <= 1)
        ints.firstOrNull() ?: 0
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        val sumL: Par<Int> = Pars.fork { Pars.unit(sum(l)) }
        val sumR: Par<Int> = Pars.fork { Pars.unit(sum(r)) }
        sumL.combine(sumR) { l, r -> l + r }.run(es)
    }
class Chapter7_1 {
    @Test
    fun main() {
        val es: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val sum = sum2(es, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        println(sum)
        es.shutdown()
    }

}