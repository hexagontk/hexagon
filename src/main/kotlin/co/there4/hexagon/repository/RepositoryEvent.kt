package co.there4.hexagon.repository

import co.there4.hexagon.events.Event
import co.there4.hexagon.util.camelToSnake

class RepositoryEvent<T : Any> (
    val source: T,
    val repositoryAction: RepositoryEventAction) :
    Event (
        source.javaClass.simpleName.camelToSnake() + '.' +
            repositoryAction.toString().toLowerCase()
    )
