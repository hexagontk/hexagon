package com.hexagonkt.http.server

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.hexagonkt.http.server.test.TestRequest
import com.hexagonkt.http.server.test.testCall
import com.hexagonkt.serialization.json.JacksonMapper
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.toObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFails

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RequestTest {

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `parse path field and query parameters to given type`() {
        val request = TestRequest(
            formParameters = mapOf("formName" to listOf("members in the team")),
            pathParameters = mapOf("teamId" to "team-hexagon"),
            queryParameters = mapOf("userId" to listOf("user1", "user2", "user3"))
        )

        val expectedData = MyCustomDto(
            teamId = "team-hexagon",
            users = listOf("user1", "user2", "user3"),
            formName = "members in the team"
        )

        val mappedData = testCall(request).request.allParameters().toObject<MyCustomDto>()

        assert(mappedData == expectedData)
    }

    @Test fun `test parsing of multiple path params query params and form fields`() {
        val formParameters = mutableMapOf("formName" to listOf("members in the team"))
        formParameters["formId"] = listOf("form1")
        formParameters["formType"] = listOf("RegistrationForm")

        val queryParams = mutableMapOf("userId" to listOf("user1", "user2", "user3"))
        queryParams["authKey"] = listOf("encryptedKey")
        queryParams["action"] = listOf("register")

        val request = TestRequest(
            formParameters = formParameters,
            pathParameters = mapOf("teamId" to "team-hexagon"),
            queryParameters = queryParams
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

        val mockRequest = testCall(request).request
        assert(mockRequest.allParameters().toObject<MyCustomDto>() == expectedData1)
        assert(mockRequest.allParameters().toObject<MyRequestDto>() == expectedData2)
    }

    @Test fun `should map the value as is if there is only value`() {
        val request = TestRequest(
            formParameters = mapOf("formName" to listOf("members in the team")),
            pathParameters = mapOf("teamId" to "team-hexagon"),
            queryParameters = mapOf("userId" to listOf("user1"))
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

        val mockRequest = testCall(request).request
        assert(mockRequest.allParameters().toObject<MyCustomDto>() == expectedData1)
        assert(mockRequest.allParameters().toObject<MyCustomDto2>() == expectedData2)
    }

    @Test fun `should honor the validations and fail gracefully by returning null object`() {
        val request = testCall(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                queryParameters = mapOf("userId" to listOf("user1"))
            )
        ).request

        // mapping fails because path parameter is mandatory
        assertFails { request.allParameters().toObject<MyCustomDto>() }
    }

    @Test fun `should fail gracefully by returning null object in case of deserialization failure`() {
        val request = testCall(
            TestRequest(
                formParameters = mapOf("formName" to listOf("members in the team")),
                pathParameters = mapOf("teamId" to "team-hexagon"),
                queryParameters = mapOf("userId" to listOf("user1"))
            )
        ).request

        // mapping fails because "Cannot deserialize instance of `java.lang.Integer`"
        assertFails { request.allParameters().toObject<Int>() }
    }

    // Test class to filter data
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

    // Test class to filter data
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
