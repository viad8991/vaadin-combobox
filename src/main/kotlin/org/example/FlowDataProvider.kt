package org.example

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.data.provider.CallbackDataProvider
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback
import com.vaadin.flow.data.provider.Query
import java.util.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.StateFlow

class FlowDataProvider<T : Any, F : Any>(
    private val source: StateFlow<Collection<T>>,
    coroutineScope: CoroutineScope,
    coroutineContext: CoroutineContext,
    filter: (T, F) -> Boolean = { _: T, f: F -> error("Filter value '$f' given but function not configured") },
    identity: (T) -> Any = { it },
) : CallbackDataProvider<T, F>(
    FetchCallback { query ->
        source.value
            .toList()
            .filter { item -> query.filter.map { filter(item, it) }.orElse(true) }
            .let { it.subList(query.offset, minOf(it.size, query.offset + query.limit)) }
            .stream()
    },
    CountCallback { query ->
        source.value
            .toList()
            .filter { item -> query.filter.map { filter(item, it) }.orElse(true) }
            .size
    },
    identity,
) {
    init {
        coroutineScope.launch(coroutineContext) {
            source.collect {
                refreshAll()
            }
        }
    }
}

class FlowDataProviderFetch<T : Any>(
    private val source: StateFlow<Collection<T>>,
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext
) : FetchCallback<T, String> {
    override fun fetch(query: Query<T, String>): Stream<T> {
        return source.value.toList()
            .let { it.subList(query.offset, minOf(it.size, query.offset + query.limit)) }
            .stream()
    }

    fun launch(target: ComboBox<T>) {
        coroutineScope.launch(coroutineContext) {
            source.collect {
                target.dataProvider.refreshAll()
            }
        }
    }
}
