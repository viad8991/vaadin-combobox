package org.example

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.ErrorEvent
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.VaadinSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

private data class VaadinDispatcher(val ui: UI) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ui.access { block.run() }
    }
}

private data class VaadinExceptionHandler(val ui: UI) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        ui.access {
            val errorHandler: ErrorHandler? = VaadinSession.getCurrent().errorHandler
            if (errorHandler != null) {
                errorHandler.error(ErrorEvent(exception))
            } else {
                throw exception
            }
        }
    }
}

fun vaadin(ui: UI = UI.getCurrent()): CoroutineContext = VaadinDispatcher(ui) + VaadinExceptionHandler(ui)
