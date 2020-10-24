package com.hexagonkt.http.server

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.hexagonkt.http.server.test.MockRequest
import com.hexagonkt.http.server.test.TestRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class RequestTest {

    @Test fun `parse path field and query parameters to given type`() {
        val request = MockRequest(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                pathParameters = mapOf("teamId" to "team-hexagon"),
                queryParameters = mapOf("userId" to listOf("user1", "user2", "user3"))
            )
        )
        val expectedData = MyCustomDto(
            teamId = "team-hexagon",
            users = listOf("user1", "user2", "user3"),
            formName = "members in the team"
        )

        val mappedData = request.parseAllParameters(MyCustomDto::class)

        assert(mappedData == expectedData)
    }

    @Test fun `test parsing of multiple path params query params and form fields`() {
        val formParameters = mutableMapOf("formName" to listOf("members in the team"))
        formParameters["formId"] = listOf("form1")
        formParameters["formType"] = listOf("RegistrationForm")

        val queryParams = mutableMapOf("userId" to listOf("user1", "user2", "user3"))
        queryParams["authKey"] = listOf("encryptedKey")
        queryParams["action"] = listOf("register")

        val request = MockRequest(
            TestRequest(
                formParameters = formParameters,
                pathParameters = mapOf("teamId" to "team-hexagon"),
                queryParameters = queryParams
            )
        )
        val expectedData1 = MyCustomDto(
            teamId = "team-hexagon",
            users = listOf("user1", "user2", "user3"),
            formName = "members in the team"
        )
        val expectedData2 = MyRequestDto(
            teamId = "team-hexagon",
            users = listOf("user1", "user2", "user3"),
            formName = "members in the team",
            action = "register",
            authKey = "encryptedKey",
            formId = "form1",
            formType = "RegistrationForm"
        )

        assert(request.parseAllParameters(MyCustomDto::class) == expectedData1)
        assert(request.parseAllParameters(MyRequestDto::class) == expectedData2)
    }

    @Test fun `should map the value as is if there is only value`() {
        val request = MockRequest(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                pathParameters = mapOf("teamId" to "team-hexagon"),
                queryParameters = mapOf("userId" to listOf("user1"))
            )
        )
        val expectedData1 = MyCustomDto(
            teamId = "team-hexagon",
            users = listOf("user1"),
            formName = "members in the team"
        )
        val expectedData2 = MyCustomDto2(
            teamId = "team-hexagon",
            user = "user1",
            formName = "members in the team"
        )

        assert(request.parseAllParameters(MyCustomDto::class) == expectedData1)
        assert(request.parseAllParameters(MyCustomDto2::class) == expectedData2)
    }

    @Test fun `should honor the validations and fail gracefully by returning null object`() {
        val request = MockRequest(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                queryParameters = mapOf("userId" to listOf("user1"))
            )
        )

        assertNull(request.parseAllParameters(MyCustomDto::class))//mapping fails because path parameter is mandatory
    }

    @Test fun `should fail gracefully by returning null object in case of deserialization failure`() {
        val request = MockRequest(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                pathParameters = mapOf("teamId" to "team-hexagon"),
                queryParameters = mapOf("userId" to listOf("user1"))
            )
        )

        assertNull(request.parseAllParameters(Int::class))//mapping fails because "Cannot deserialize instance of `java.lang.Integer`"
    }

    //Test class to filter data
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MyCustomDto(
        @JsonProperty("teamId") val teamId: String,
        @JsonProperty("userId") val users: List<String>,
        @JsonProperty("formName") val formName: String
    ) {
        init {
            require(teamId.isNotBlank())
        }
    }

    //Test class to filter data
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MyCustomDto2(
        @JsonProperty("teamId") val teamId: String,
        @JsonProperty("userId") val user: String,
        @JsonProperty("formName") val formName: String
    ) {
        init {
            require(teamId.isNotBlank())
        }
    }

    //Test class to filter data
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MyRequestDto(
        @JsonProperty("teamId") val teamId: String,
        @JsonProperty("formId") val formId: String,
        @JsonProperty("formType") val formType: String,
        @JsonProperty("userId") val users: List<String>,
        @JsonProperty("formName") val formName: String,
        @JsonProperty("action") val action: String,
        @JsonProperty("authKey") val authKey: String,
    ) {
        init {
            require(teamId.isNotBlank())
        }
    }
}
