package co.there4.hexagon.repository

import co.there4.hexagon.events.Event
import kotlin.reflect.KClass

class RepositoryIdEvent<T : Any, K : Any> (
    source: KClass<T>,
    val id: K,
    val repositoryAction: RepositoryEventAction) :
    Event (source.simpleName + '.' + repositoryAction.toString())
