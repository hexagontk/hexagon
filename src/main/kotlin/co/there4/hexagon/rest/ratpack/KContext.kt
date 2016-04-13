package co.there4.hexagon.rest.ratpack

import ratpack.handling.Context

class KContext (val delegate: Context) : Context by delegate
