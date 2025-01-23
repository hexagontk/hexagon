package com.hexagontk.serialization.jackson.xml

import com.hexagontk.core.*
import com.hexagontk.core.text.toStream
import com.hexagontk.serialization.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class XmlTest {

    data class Player(
        val name: String,
        val number: Int,
        val category: ClosedRange<Int>
    )

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Xml)
    }

    private fun Player.convert(): Map<*, *> =
        mapOf(
            Player::name.name to name,
            Player::number.name to number,
            Player::category.name to mapOf(
                ClosedRange<*>::start.name to category.start,
                ClosedRange<*>::endInclusive.name to category.endInclusive,
            )
        )

    private fun Map<*, *>.convert(): Player =
        Player(
            name = requirePath(Player::name),
            number = requireString(Player::number).toInt(),
            category = requirePath<Map<String, String>>(Player::category).let { map ->
                val start = map.require(ClosedRange<*>::start.name).toInt()
                val endInclusive = map.require(ClosedRange<*>::endInclusive.name).toInt()
                start..endInclusive
            }
        )

    @Test fun `XML can be parsed to collections` () {
        val xml =
            """
            <project>
              <name>Kotlin POM</name>
              <description>Kotlin's starter POM.</description>

              <repositories>
                <repository>
                  <snapshots>
                    <enabled>false</enabled>
                  </snapshots>
                  <id>central</id>
                </repository>
              </repositories>

              <dependencies>
                <dependency>
                  <groupId>org.junit.jupiter</groupId>
                  <artifactId>junit</artifactId>
                  <scope>test</scope>
                </dependency>
                <dependency>
                  <groupId>org.jetbrains.kotlin</groupId>
                  <artifactId>kotlin-test</artifactId>
                  <scope>test</scope>
                </dependency>
              </dependencies>

              <alien property="val">text</alien>
            </project>
            """
        val parse = xml.parse(Xml)
        val collection = parse as Map<*, *>

        assertEquals("Kotlin POM", collection["name"])
        assertEquals("central", collection.getPath("repositories", "repository", "id"))
        assertEquals("junit", collection.getPath("dependencies", "dependency", 0, "artifactId"))
        assertEquals("val", collection.getPath("alien", "property"))
        assertEquals("text", collection.getPath("alien", ""))
        assertEquals(
            "kotlin-test",
            collection.getPath("dependencies", "dependency", 1, "artifactId")
        )
    }

    @Test
    fun `XML can be parsed to collections (bis)` () {
        val xml =
            """<?xml version="1.0" encoding="UTF-8"?>

            <beans
              xmlns="http://www.springframework.org/schema/beans"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xmlns:camel="http://camel.apache.org/schema/spring"
              xsi:schemaLocation="
                http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
                http://camel.apache.org/schema/spring
                http://camel.apache.org/schema/spring/camel-spring.xsd">

              <bean id="header" class="org.example.Processor"/>

              <camelContext
                xmlns="http://camel.apache.org/schema/spring"
                id="service.v2.0.0">

                <restConfiguration
                  component="servlet"
                  producerApiDoc="service-v2.0.0-swagger.json"
                  bindingMode="auto"
                  enableCORS="true"/>

                <rest path="/v1/path" produces="application/json" id="service.restlet">
                  <get uri="/uri" id="lS2">
                    <to uri="direct-vm:p-l.lS2"/>
                  </get>
                  <get uri="/uri/{recordId}" id="gS2">
                    <param name="recordId" type="path" required="true"/>
                    <to uri="direct-vm:p-g.gS2"/>
                  </get>
                  <get uri="/uri/{recordId}/component" id="gSS2">
                    <to uri="direct-vm:p-l.gSS2" />
                  </get>
                  <post uri="/uri" id="lS2">
                    <to uri="direct-vm:p-l.lS2"/>
                  </post>
                  <post uri="uri/{recordId}/tag" id="cL2">
                    <to uri="direct-vm:p-c.cL2"/>
                  </post>
                </rest>
              </camelContext>
            </beans>
            """

        val collection = xml.parse(Xml) as Map<*, *>

        assertEquals("header", collection.getPath("bean", "id"))
        assertEquals(2, (collection.getPath("camelContext", "rest", "post") as? List<*>)?.size)
        assertEquals(3, (collection.getPath("camelContext", "rest", "get") as? List<*>)?.size)
    }

    @Suppress("UNCHECKED_CAST") // Required by test
    @Test fun `XML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.convert().serialize(Xml)
        val deserializedPlayer = serializedPlayer.parseMap(Xml).convert()

        assertEquals(deserializedPlayer.name, player.name)
        assertEquals(deserializedPlayer.number, player.number)
        assertEquals(deserializedPlayer.category.start, player.category.start)
        assertEquals(deserializedPlayer.category.endInclusive, player.category.endInclusive)

        val players = mapOf(
            "players" to listOf(
                Player("Michael", 23, 18..65),
                Player("Magic", 32, 18..65),
            )
        )
        val serializedPlayers = players
            .let { it + ("players" to it["players"]?.map { p -> p.convert() }) }
            .serialize(Xml)
        val parse = serializedPlayers.parse(Xml) as Map<*, List<Any>>
        val deserializedPlayers = parse.entries.first().value.map { (it as Map<*, *>).convert() }

        val first = deserializedPlayers.first()
        val last = deserializedPlayers.last()
        assertEquals(first.name, players.require("players").first().name)
        assertEquals(first.number, players.require("players").first().number)
        assertEquals(first.category.start, players.require("players").first().category.start)
        assert(
            players.require("players").first().category.endInclusive == first.category.endInclusive
        )

        assertEquals(last.name, players.require("players").last().name)
        assertEquals(last.number, players.require("players").last().number)
        assertEquals(last.category.start, players.require("players").last().category.start)
        assert(
            players.require("players").last().category.endInclusive == last.category.endInclusive
        )
    }

    @Test fun `Parse valid XML` () {
        val parse = """
            <ArrayList>
                <item>
                    <a>b</a>
                </item>
                <item>
                    <b>c</b>
                </item>
                <item>
                    <c>d</c>
                </item>
            </ArrayList>
        """.trimIndent().toStream().parse(Xml) as Map<*, *>
        assertEquals("b", parse.getPath("item", 0, "a"))
    }

    @Test fun `Object can be serialized to stream`() {
        val xml = String(Xml.serializeBytes(mapOf("a" to listOf(1,2,3))))
        assertTrue(xml.contains("<a>1</a>"))
    }
}
