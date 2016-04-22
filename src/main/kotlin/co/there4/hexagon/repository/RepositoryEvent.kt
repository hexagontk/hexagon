package co.there4.hexagon.repository

import co.there4.hexagon.events.Event

class RepositoryEvent<T : Any> (
    val source: T,
    val repositoryAction: RepositoryEventAction) :
    Event (source.javaClass.simpleName + '.' + repositoryAction.toString())
