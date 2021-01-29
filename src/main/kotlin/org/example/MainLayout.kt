package org.example

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.content
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h3
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.sortProperty
import com.github.mvysny.karibudsl.v10.textArea
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.util.SharedUtil
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Push
@Theme(Lumo::class, variant = Lumo.DARK)
class AppShellConfiguratorForced : AppShellConfigurator

@Route("")
class MainLayout : KComposite(), CoroutineScope {
    private val uiJob = SupervisorJob()
    private val uiCoroutineContext = vaadin()

    private val server: Server = ServerImpl()

    private val root = ui {
        horizontalLayout {
            verticalLayout {
                setSizeFull()
                content { left }
                width = "20vw"

                val gridDP = FlowDataProvider<Event, Unit>(
                    server.event(),
                    this@MainLayout,
                    coroutineContext
                )

                h3("log:")
                grid<Event>(gridDP) {
                    height = "90vh"
                    addColumnFor(Event::text)
                }
            }
            verticalLayout {
                setSizeFull()
                content { center() }
                width = "60vw"

                val gridDP = FlowDataProvider<DataGridData, Category>(
                    server.data(),
                    this@MainLayout,
                    coroutineContext,
                    filter = { d, f -> d.category == f }
                )

                val gridWrapperDP = gridDP.withConfigurableFilter()

                horizontalLayout {
                    val flow = FlowDataProviderFetch(
                        server.categoryFlow(),
                        this@MainLayout,
                        coroutineContext
                    )

                    div {
                        h3("set (bad)")
                        comboBox<Category> {
                            val category = server.categorySet()
//                            setItems(server.categorySet())
                            setItemLabelGenerator(Category::name)
                            addValueChangeListener { event ->
                                if (event.value != null) gridWrapperDP.setFilter(event.value)
                            }
//                            value = category.first()
                        }
                    }
                    div {
                        h3("flow (best)")
                        comboBox<Category> {
                            setItems(flow)
                            setItemLabelGenerator(Category::name)
                            addValueChangeListener { event ->
                                if (event.value != null) gridWrapperDP.setFilter(event.value)
                            }
                        }.apply { flow.launch(this) }
                    }
                    div {
                        h3("flow click (normal)")
                        comboBox<Category> {
                            setItems(flow)
                            setItemLabelGenerator(Category::name)
                            addFocusListener {
                                dataProvider.refreshAll()
                            }
                            addValueChangeListener { event ->
                                if (event.value != null) gridWrapperDP.setFilter(event.value)
                            }
                        }
                    }
                }

                grid<DataGridData>(gridWrapperDP) {
                    height = "80vh"
                    addColumnFor(DataGridData::data)
                    addColumnKeyFor(DataGridData::category, Category::name, { it.name })
                    addColumnFor(DataGridData::time, LocalDateTimeRenderer { it.time })
                }
            }
            verticalLayout {
                setSizeFull()
                content { right }
                width = "20vw"

                h3("description:")
                textArea {
                    setSizeFull()
                    isReadOnly = true
                    value = """1) the first combobox is very terrible. If a new element has appeared on the server, 
                        |we will not know about it. (commit, not work now)
                        |
                        |2) the second combobox is the best, because it is updated before the user uses it. But the
                        |problem is that if you leave it open, it "glares" very much.
                        |
                        |3) it does not glare as often, but this is not the best option, because the user may not know 
                        |that an element has been added to the server
                        |
                        |2 and 3) we also cannot set the first element if the data comes to the grid faster than the 
                        |filter (filter is combobox), so everything is displayed in the grid (although it would be 
                        |better if nothing was displayed)
                        |""".trimMargin()
                }
            }
        }
    }
    override val coroutineContext: CoroutineContext
        get() = uiJob + uiCoroutineContext
}

fun <T, V, N> (@VaadinDsl Grid<T>).addColumnKeyFor(
    property: KProperty1<T, V>,
    childrenProperty: KProperty1<V, N>,
    converter: (V) -> Any? = { it },
    sortable: Boolean = false,
    block: (@VaadinDsl Grid.Column<T>).() -> Unit = {},
): Grid.Column<T>? =
    addColumn { converter(property.get(it)) }.apply {
        key = childrenProperty.name
        if (sortable) sortProperty = property
        setHeader(SharedUtil.camelCaseToHumanFriendly(childrenProperty.name))
        block()
    }
