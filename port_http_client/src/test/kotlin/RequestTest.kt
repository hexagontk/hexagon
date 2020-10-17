package com.hexagonkt.http.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.hexagonkt.http.Method
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertNull

@TestInstance(PER_CLASS)
class RequestTest {

    @Test fun `form and path parmeters are mapped as expected`() {
        val pathParameters =
            mapOf(Pair("teamId", listOf("team1")), Pair("productId", listOf("99887766")))
        val userIdList = listOf("user1", "user2", "user3")
        val request = Request(
            method = Method.POST,
            path = "/{teamId}/{productId}",
            pathParameters = pathParameters,
            formParameters = mapOf(Pair("userId", userIdList))
        )
        val expectedObject = FilterUsers("team1", userIdList)

        val filterUsers = request.parseAllParameters(FilterUsers::class)

        assert(filterUsers == expectedObject)
    }

    @Test fun `should return null object if there is nothing to map`() {
        val request = Request(
            method = Method.POST,
            path = "/{somePathParam}/",
            pathParameters = emptyMap(),
            formParameters = emptyMap()
        )

        val filterUsers = request.parseAllParameters(FilterUsers::class)

        assertNull(filterUsers)
    }

    @Test fun `should retun null object in case mapping failed due to restriction in the given data class`() {
        //teamId is mandatory for given FilterUsers class, hence mapping will fail
        val pathParameters = mapOf(Pair("productId", listOf("99887766")))
        val userIdList = listOf("user1", "user2", "user3")
        val request = Request(
            method = Method.POST,
            path = "/{teamId}/{productId}",
            pathParameters = pathParameters,
            formParameters = mapOf(Pair("userId", userIdList))
        )
        val filterUsers = request.parseAllParameters(FilterUsers::class)

        assertNull(filterUsers)
    }

    @Test fun `should handle exception and retun null object in case of a mapping failure`() {
        val request = Request(
            method = Method.POST,
            path = "/{somePathParam}/",
            pathParameters = emptyMap(),
            formParameters = emptyMap()
        )

        val intValueClass = request.parseAllParameters(Int::class)

        assertNull(intValueClass)
    }

}

//Test class to filter users
@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterUsers(
    @JsonProperty("teamId") val teamId: String,
    @JsonProperty("userId") val userId: List<String>
) {
    init {
        require(teamId.isNotBlank())
    }
}
