package ru.otus.flow.start

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.otus.flow.Animal
import ru.otus.flow.Animal.Bird
import ru.otus.flow.Animal.Cat
import ru.otus.flow.Animal.Dog
import ru.otus.flow.Animal.Fish
import ru.otus.flow.NumberWithType
import ru.otus.flow.start.FlowPractice.FakeAnimalApi.Callback
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class FlowPractice {

    @Before
    fun before() {
        println("\n--------------------")
        println("--- Test started ---\n")
    }

    @After
    fun after() {
        println("\n--- Test ended -----")
        println("--------------------")
    }

    @Test
    fun channel_basic() {
        runBlocking {

        }
    }

    @Test
    fun channel_consumeEach() {
        runBlocking {

        }
    }

    @Test
    fun channel_produce() {
        runBlocking {

        }
    }

    @Test
    fun channel_produce_with_capacity() {
        runBlocking {

        }
    }

    @Test
    fun channel_produce_conflate() {
        runBlocking {

        }
    }

    @Test
    fun channel_produce_rendezvous() {
        runBlocking {

        }
    }

    @Test
    fun channel_produce_unlimited() {
        runBlocking {

        }
    }

    @Test
    fun flow_builders() {
        runBlocking {

        }
    }

    @Test
    fun flow_basic_intermediate_operators() {
        runBlocking {

        }
    }

    @Test
    fun flow_retry_operator() {
        runBlocking {

        }
    }

    @Test
    fun flow_scan_operator() {
        runBlocking {

        }
    }

    @Test
    @FlowPreview
    fun flow_debounce_operator() {
        runBlocking {

        }
    }

    @Test
    fun flow_fold_operator() {
        runBlocking {

        }
    }

    @Test
    fun flow_runningFold_operator() {
        runBlocking {

        }
    }

    suspend fun simulateNetworkCall(query: String) {
        println("  Simulating network call...")
        delay(500)  // Mock network delay
        println("  Done processing '$query'")
    }

    @Test
    fun flow_takeWhile_operator() {
        runBlocking {

        }
    }

    @Test
    fun flow_distinctUntilChangedBy() {
        runBlocking {

        }
    }

    @Test
    fun flow_distinctUntilChangedBy_ver2() {
        runBlocking {

        }
    }

    private fun flowOfNumbers(n: Int, delay: Long = 0L, withLog: Boolean = false) = flow {
        for (i in 1..n) {
            delay(delay)
            if (withLog) println("  Before emit $i")
            emit(i)
            if (withLog) println("  After emit $i")
        }
    }

    private fun flowOfAnimals(delay: Long = 0L, withLog: Boolean = false) = flow {
        delay(delay)
        if (withLog) println("  Before emit Cat")
        emit(Cat)
        if (withLog) println("  After emit Cat")
        delay(delay)
        if (withLog) println("  Before emit Dog")
        emit(Dog)
        if (withLog) println("  After emit Dog")
        delay(delay)
        if (withLog) println("  Before emit Fish")
        emit(Fish)
        if (withLog) println("  After emit Fish")
        delay(delay)
        if (withLog) println("  Before emit Bird")
        emit(Bird)
        if (withLog) println("  After emit Bird")
    }

    @Test
    fun flow_flatMap() {
        runBlocking {

        }
    }

    @Test
    fun flow_trasformLatest() {

    }

    private fun search(query: String): Flow<List<String>> = flow {
        delay(500) // Имитация API-запроса
        emit(listOf("$query-result-1", "$query-result-2"))
    }

    @Test
    fun flow_customOperator() {
        runBlocking {
            //distinctUntilTime
        }
    }

    @Test
    fun flow_customOperator_ver2() {
        runBlocking {

        }
    }

    @Test
    fun flow_combining() {
        runBlocking {

        }
    }

    @Test
    fun flow_with_buffer() {
        runBlocking {

        }
    }

    @Test
    fun flow_channel_flow() {
        runBlocking {

        }
    }

    class FakeAnimalApi {
        interface Callback {
            fun onDataReady(animals: List<Animal>)
        }
        suspend fun getAnimals(callback: Callback) {
            delay(300)
            callback.onDataReady(Animal.values().toMutableList().apply { remove(Cat) })
            delay(500)
            callback.onDataReady(Animal.values().toList())
        }
    }

    @Test
    fun flow_callback_flow() {
        runBlocking {

        }
    }

    @Test
    fun sharedFlow() {
        runBlocking {

        }
    }

    @Test
    fun stateFlow() {
        runBlocking {

        }
    }
}