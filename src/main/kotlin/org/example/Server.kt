package org.example

import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Event(
    val text: String
)

data class DataGridData(
    val data: String,
    val category: Category,
    val time: LocalDateTime
)

data class Category(
    val name: String
)

// imitation gRPC or rSocket server with flow
interface Server {
    fun event(): StateFlow<List<Event>>
    fun data(): StateFlow<Set<DataGridData>>
    fun categoryFlow(): StateFlow<Set<Category>>
    fun categorySet(): Set<Category>
}

class ServerImpl : Server {
    private val eventFlow = MutableStateFlow<List<Event>>(mutableListOf())
    private val dataFlow = MutableStateFlow<Set<DataGridData>>(emptySet())
    private val categoryFlow = MutableStateFlow<Set<Category>>(emptySet())

    private val names = listOf("Matt", "Ivan", "Evelyn", "Pasha", "Vlad", "Robert", "Jamie")

    private val exampleCategory = mutableSetOf(
        Category("Milky Way"),
        Category("Arcanum"),
    )

    private val exampleDataGridData = setOf(
        DataGridData("qwerrty", exampleCategory.elementAt(0), LocalDateTime.now()),
        DataGridData("143123413", exampleCategory.elementAt(1), LocalDateTime.now()),
        DataGridData("asdhfas13413143", exampleCategory.elementAt(0), LocalDateTime.now()),
        DataGridData("j1h23g4j123g4j1", exampleCategory.elementAt(1), LocalDateTime.now()),
    )

    private val events = mutableListOf<Event>()

    override fun event(): StateFlow<List<Event>> = eventFlow.asStateFlow()
    override fun data(): StateFlow<Set<DataGridData>> = dataFlow.asStateFlow()
    override fun categoryFlow(): StateFlow<Set<Category>> = categoryFlow.asStateFlow()
    override fun categorySet(): Set<Category> = exampleCategory

    init {
        GlobalScope.launch {
            // Bad inet
            delay(3000L)

            dataFlow.value = setOf(*exampleDataGridData.toTypedArray())
            categoryFlow.value = setOf(*exampleCategory.toTypedArray())

            events.add(Event("Set first data"))
            eventFlow.value = listOf(*events.toTypedArray())

            for (i in 0..50) {
                delay(2000L)
                exampleCategory.add(Category("categ$i"))
                categoryFlow.value = setOf(*exampleCategory.toTypedArray())

                val set = dataFlow.value.toMutableSet()
                set.add(DataGridData("add $i", exampleCategory.elementAt(Random.nextInt(exampleCategory.indices)), LocalDateTime.now()))
                dataFlow.value = setOf(*set.toTypedArray())

                events.add(Event("${names[Random.nextInt(names.indices)]} add new data and category $i"))
                eventFlow.value = listOf(*events.toTypedArray())
            }
        }
    }
}