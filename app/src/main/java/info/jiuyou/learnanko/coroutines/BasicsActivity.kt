package info.jiuyou.learnanko.coroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import kotlin.coroutines.experimental.CoroutineContext

class BasicsActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basics)
        test()
        info("==========end=============")
    }

    fun test() = runBlocking {
        var cur = numbersFrom(context, 2)
        for (i in 1..10) {
            val prime = cur.receive()
            println(prime)
            cur = filter(context, cur, prime)
        }
    }

    fun filter(context: CoroutineContext, numbers: ReceiveChannel<Int>, prime: Int) = produce(context) {
        println("=================filter==================$prime")
        for (x in numbers) {
            println("=====x===$x===prime===$prime")
            send(x)
            //if (x % prime != 0){
            //    send(x)
            //}
        }
    }

    fun numbersFrom(context: CoroutineContext, start: Int) = produce(context) {
        var x = start
        while (true) send(x++)

    }
}

