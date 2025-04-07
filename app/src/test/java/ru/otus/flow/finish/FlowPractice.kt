package ru.otus.flow.finish

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.takeWhile
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
import ru.otus.flow.finish.FlowPractice.FakeAnimalApi.Callback
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
            val channel = Channel<Animal>()
            launch {
                channel.send(Cat)
            }

            launch {
                val animal = channel.receive()
                println("$animal received") // Cat received
            }
        }
    }

    @Test
    fun channel_consumeEach() {
        runBlocking {
            val channel = Channel<Animal>()
            launch {
                channel.send(Cat)
                channel.send(Dog)
                channel.send(Fish)
                channel.close()
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce() {
        runBlocking {
            val channel = produce {
                send(Cat)
                send(Dog)
                send(Fish)
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_with_capacity() {
        runBlocking {
            val channel = produce(capacity = 2) {
                send(Cat)
                println("Cat has been sent")
                send(Dog)
                println("Dog has been sent")
                send(Fish)
                println("Fish has been sent")
            }

            launch {
                delay(100)
                println("${channel.receive()} received")
                delay(100)
                println("${channel.receive()} received")
                delay(100)
                println("${channel.receive()} received")
            }
        }
    }

    @Test
    fun channel_produce_conflate() {
        runBlocking {
            val channel = produce(capacity = CONFLATED) {
                send(Cat)
                println("Cat has been sent")
                send(Dog)
                println("Dog has been sent")
                send(Fish)
                println("Fish has been sent")
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_rendezvous() {
        runBlocking {
            var channel : ReceiveChannel<Animal> = Channel()
            launch {
                channel = produce(capacity = RENDEZVOUS) {
                    send(Cat)
                    println("Cat has been sent")
                    send(Dog)
                    println("Dog has been sent")
                    send(Fish)
                    println("Fish has been sent")
                }
            }

            delay(100)

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_unlimited() {
        runBlocking {
            var channel : ReceiveChannel<Animal> = Channel()
            launch {
                channel = produce(capacity = UNLIMITED) {
                    send(Cat)
                    println("Cat has been sent")
                    send(Dog)
                    println("Dog has been sent")
                    send(Fish)
                    println("Fish has been sent")
                }
            }

            delay(100)

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun flow_builders() {
        runBlocking {
            println("1)")
            flow {
                emit(Cat)
                emit(Dog)
            }
                .collect { animal -> println("$animal received") }

            println("2)")
            flowOf(Cat, Dog).collect { animal -> println("$animal received") }

            println("3)")
            Animal.values().asFlow().collect { animal -> println("$animal received") }
        }
    }

    @Test
    fun flow_basic_intermediate_operators() {
        runBlocking {
            Animal.values().asFlow()
                .filterNot { animal -> animal == Fish }
                .map { animal -> "Robot of $animal"}
                .flatMapConcat { robot ->
                    flow {
                        emit("head of $robot")
                        emit("body of $robot")
                        emit("hands of $robot")
                        emit("legs of $robot")
                    }
                }
                .onStart {
                    println("Starting work")
                }
                .onEach {  part -> println("$part received onEach") }
                .collect { part -> println("$part received in collect") }
        }
    }

    @Test
    fun flow_scan_operator() {
        runBlocking {
            flowOfNumbers(n = 6, delay = 20L, withLog = false)
                .scan(0) { acc, next -> acc + next }
                .collect { println(it) }
        }
    }

    @Test
    @FlowPreview
    fun flow_debounce_operator() {
        runBlocking {
            // Simulate user typing in a search field
            val searchQueryFlow = flow {
                emit("A")       // Immediate emission
                delay(100)
                emit("AB")      // Fast follow-up
                delay(200)
                emit("ABC")     // Slightly slower
                delay(300)      // Longer pause
                emit("ABCD")    // This should trigger
                delay(300)
                emit("ABCDE")   // Too fast - should be ignored
            }

            searchQueryFlow
                .debounce(300)  // Wait 300ms after last emission
                .collect { query ->
                    println("[${System.currentTimeMillis()}] Performing search for: '$query'")
                    // In real app, this would call your API
                    simulateNetworkCall(query)
                }
        }
    }

    /*
    * acc=0, value=1
    * acc=1, value=2
    * acc=3, value=3
    * acc=6, value=4
    * acc=10, value=5
    * Итоговая сумма: 15
    * */
    @Test
    fun flow_fold_operator() {
        runBlocking {
            val sum = flowOf(1, 2, 3, 4, 5)
                .fold(0) { acc, value ->
                    println("acc=$acc, value=$value")
                    acc + value
                }

            println("Итоговая сумма: $sum")
        }
    }

    /*
    * Start:
    * Start: A
    * Start: A B
    * Start: A B C
    * Start: A B C D
    * */
    @Test
    fun flow_runningFold_operator() {
        runBlocking {
            flowOf("A", "B", "C", "D")
                .runningFold("Start:") { acc, value ->
                    "$acc $value"
                }
                .collect { println(it) }
            /*
            * flowOfNumbers(n = 5, delay = 100)
                .runningFold(0) { acc, num ->
                    println("Добавляем $num к $acc")
                    acc + num
                }
                .collect { println("Текущая сумма: $it") }
            * */
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
            flowOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .takeWhile { number -> number < 7 }
                .collect {
                    println(it)
                }
        }
    }

    @Test
    fun flow_retry_operator() {
        runBlocking {
            val retryFlow = flow {
                emit(1)
                throw IllegalArgumentException()
            }.retry(3)

            retryFlow.collect { println(it) }
        }
    }

    /*    @Test
        fun flow_windowed_operator() {
            runBlocking {
                flowOfNumbers(n = 5, delay = 100)
                    .windowed(size = 2, step = 3)  // size = размер окна, step = шаг
                    .collect { window ->
                        println("Окно: $window")
                    }
            }
        }*/

    @Test
    fun flow_distinctUntilChangedBy() {
        runBlocking {
            flowOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5)
                .distinctUntilChangedBy { it % 2 }
                .collect {
                    println(it)
                }
        }
    }

    /*
    * NumberWithType(num=1, type=Odd)
    * NumberWithType(num=2, type=Even)
    * NumberWithType(num=5, type=Odd)  // 3 и 4 пропущены, так как их type совпадал с предыдущими
    * */
    @Test
    fun flow_distinctUntilChangedBy_ver2() {
        runBlocking {
            flow {
                emit(NumberWithType(1, "Odd"))
                emit(NumberWithType(2, "Even"))
                emit(NumberWithType(3, "Odd"))  // type совпадает с первым элементом
                emit(NumberWithType(4, "Even")) // type совпадает со вторым
                emit(NumberWithType(5, "Odd"))
            }
                .distinctUntilChangedBy { it.type } // Сравниваем только поле type
                .collect { println(it) }
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
            val flow1 = flowOfNumbers(n = 3, delay = 30L)
            val flow2 = flowOfAnimals(delay = 20L)

            println("\nflatMapConcat")
            flow1
                .flatMapConcat { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("    -> $it received") }

            println("\nflatMapMerge")
            flow1
                .flatMapMerge { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("$it received") }

            println("\nflatMapLatest")
            flow1
                .flatMapLatest { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("$it received") }
        }
    }

    /*
    * Loading...  // Для "AB"
    * ["AB-result-1", "AB-result-2"]
    * Loading...  // Для "ABC"
    * ["ABC-result-1", "ABC-result-2"]
    * Loading...  // Для "B" (проигнорирован, длина < 2)
    * Loading...  // Для "C" (проигнорирован)
    * */
    @Test
    fun flow_trasformLatest() {
        runBlocking {
            val searchQueryFlow = flowOf("A", "AB", "B", "C","ABC")

            searchQueryFlow
                .transformLatest { query ->
                    if (query.length >= 2) {  // Фильтрация прямо в transformLatest
                        emit("Loading...")
                        search(query).collect { results ->
                            emit(results)
                        }
                    }
                }
                .collect { println(it) }
        }
    }

    private fun search(query: String): Flow<List<String>> = flow {
        //delay(500) // Имитация API-запроса
        emit(listOf("$query-result-1", "$query-result-2"))
    }

    @Test
    fun flow_customOperator() {
        runBlocking {
            val flow = flow {
                emit(Cat)
                emit(Dog)
                delay(310)
                emit(Dog)
                emit(Fish)
                emit(Fish)
                emit(Fish)
                emit(Bird)
            }

            flow.distinctUntilTime(300)
                .collect { animal ->
                    println("$animal collected")
                }
        }
    }

    @Test
    fun flow_customOperator_ver2() {
        runBlocking {
            val startTime = System.currentTimeMillis()

            flowOfNumbers(n = 10, delay = 100)
            flowOfNumbers(n = 10, delay = 100)
                .onEach { println("[${System.currentTimeMillis() - startTime}ms] Emitted: $it") }
                .throttleFirst(300) // Пропускаем не чаще чем раз в 300мс
                .collect { println("[${System.currentTimeMillis() - startTime}ms] Processed: $it") }
        }
    }

    fun <T> Flow<T>.distinctUntilTime(time: Long): Flow<T> {
        return flow {
            var lastTime = System.currentTimeMillis()
            var lastValue: T? = null
            collect { value ->
                if (value != lastValue || (System.currentTimeMillis() - lastTime) >= time) {
                    emit(value)
                }
                lastValue = value
                lastTime = System.currentTimeMillis()
            }
        }
    }

    fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> = flow {
        var lastTime = 0L // время последнего эмита
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }

    fun <T> Flow<T>.throttleFirstV2(periodMillis: Long): Flow<T> = channelFlow {
        var lastTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                send(value)
            }
        }
    }

    @Test
    fun flow_combining() {
        runBlocking {
            val flow1 = flowOfNumbers(n = 3, delay = 10L)
            val flow2 = flowOfAnimals(delay = 50L)

            println("Zip")
            flow1.zip(flow2) { index, animal ->
                "$index-$animal"
            }
                .collect { zipped ->
                    println("$zipped collected")
                }

            println("combine")
            combine(
                flow1,
                flow2
            ) { index, animal ->
                "$index-$animal"
            }
                .collect { combined ->
                    println("$combined collected")
                }

            println("merge")
            merge(
                flow1,
                flow2
            )
                .collect { merged ->
                    println("$merged collected")
                }
        }
    }

    @Test
    fun flow_with_buffer() {
        runBlocking {
            val flow = flowOfAnimals(delay = 90L, withLog = true)

            val time = measureTimeMillis {
                flow
                    .buffer()
                    .collect { animal ->
                        delay(100L)
                        println("$animal collected")
                    }
            }
            println("Took $time millis")
        }
    }

    @Test
    fun flow_channel_flow() {
        runBlocking {
            val flow = channelFlow {
                Animal.values().asFlow()
                    .collect { animal ->
                        if (animal == Fish) {
                            channel.close()
                        } else {
                            if (!isClosedForSend) send(animal)
                        }
                    }
                awaitClose()
            }

            val time = measureTimeMillis {
                flow.collect { animal ->
                    delay(80)
                    println("$animal collected")
                }
            }
            println("Took $time millis")
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
            val api = FakeAnimalApi()
            val flow = callbackFlow {
                val callback = object : Callback {
                    override fun onDataReady(animals: List<Animal>) {
                        println("onDataReady $animals")
                        animals.forEach { animal ->
                            trySend(animal)
                        }
                        if (animals.contains(Cat)) {
                            channel.close()
                        }
                    }
                }

                api.getAnimals(callback)

                awaitClose {
                    println("Releasing resources")
                }
            }

            flow.collect { animal ->
                println("$animal collected")
            }
        }
    }

    @Test
    fun sharedFlow() {
        runBlocking {
            val flow = MutableSharedFlow<Animal>(replay = 1, extraBufferCapacity = 2)

            val job1 = launch {
                flow.tryEmit(Cat)
                delay(100)
                flow.tryEmit(Dog)
                delay(100)
                flow.tryEmit(Fish)
                delay(100)
            }

            delay(50)

            val job2 = launch {
                flow.collect { animal ->
                    println("$animal collected")
                }
            }

            delay(1000)
            job1.cancel()
            job2.cancel()
        }
    }


    @Test
    fun sharedFlow_onSubscription() {
        runBlocking {
            // Создаем SharedFlow с replay = 1, чтобы новый подписчик получил последнее значение
            val sharedFlow = MutableSharedFlow<Int>(replay = 1).apply {
                onSubscription {
                    println("Новый подписчик подключился!")
                    // Можно отправить начальное значение новому подписчику
                    tryEmit(-1)
                }
            }

            // Запускаем корутину, которая будет эмитить значения
            launch {
                repeat(3) { i ->
                    delay(300)
                    sharedFlow.emit(i + 1)
                    println("Emitted: ${i + 1}")
                }
            }

            // Подписываемся с задержкой, чтобы часть значений уже была отправлена
            delay(500)
            println("--- Начинаем коллекцию ---")
            sharedFlow.collect { value ->
                println("Collect: $value")
            }
        }
    }

    @Test
    fun stateFlow() {
        runBlocking {
            val flow = MutableStateFlow(Cat)

            val job1 = launch {
                delay(100)
                flow.tryEmit(Dog)
                delay(100)
                flow.tryEmit(Fish)
                delay(100)
            }

            delay(120)
            println("${flow.value} checked")

            val job2 = launch {
                flow.collect { animal ->
                    println("$animal collected")
                }
            }

            delay(1000)
            println("${flow.value} checked")

            delay(1000)
            job1.cancel()
            job2.cancel()
        }
    }
}