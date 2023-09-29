package com.example.fp_practice_with_kotlin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fp_practice_with_kotlin.ui.theme.FP_Practice_With_KotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FP_Practice_With_KotlinTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        solve()
    }
}


fun print(msg: String) {
    Log.d("fp", msg)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FP_Practice_With_KotlinTheme {
        Greeting("Android")
    }
}









object Example {
    private fun abs(n: Int): Int
        = if (n < 0) -n else n
    fun formatAbs(x: Int): String = "The absolute value of %d is %d".format(x, abs(x))
    private fun factorial(i: Int): Int {
        tailrec fun go(n: Int, acc: Int): Int
            = if (n <= 0) acc else go(n-1, n * acc)
        return go(i, 1)
    }
    fun formatFactorial(x: Int): String {
        val msg = "The factorial of %d is %d"
        return msg.format(x, factorial(x))
    }
}
fun solve() {
    print(Example.formatAbs(-42))
    print(Example.formatFactorial(7))
}

fun fib(i: Int): Int {
    if (i > 0).not() {

    }
}