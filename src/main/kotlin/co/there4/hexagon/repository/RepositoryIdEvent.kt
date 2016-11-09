package co.there4.hexagon.repository

import co.there4.hexagon.events.Event
import kotlin.reflect.KClass

@Suppress("CanBeParameter")
class RepositoryIdEvent<T : Any, out K : Any> (
    source: KClass<T>,
    val id: K,
    val repositoryAction: RepositoryEventAction) :
        Event (source.simpleName + '.' + repositoryAction.toString())
