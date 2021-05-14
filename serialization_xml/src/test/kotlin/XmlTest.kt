package com.hexagonkt.serialization

import com.hexagonkt.helpers.get
import com.hexagonkt.helpers.println
import com.hexagonkt.helpers.toStream
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class XmlTest {

    enum class DeviceOs { ANDROID, IOS }

    data class Device(
        val id: String,
        val brand: String,
        val model: String,
        val os: DeviceOs,
        val osVersion: String,

        val alias: String = "$brand $model"
    )

    data class Players(
        val players: List<Player>
    )

    data class Player(val name: String, val number: Int, val category: ClosedRange<Int>)

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Xml)
    }

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
                  <name>jcenter</name>
                  <url>https://jcenter.bintray.com</url>
                </repository>
              </repositories>

              <dependencies>
                <dependency>
                  <groupId>org.junit.jupiter</groupId>
                  <artifactId>junit-jupiter</artifactId>
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
        val collection = xml.parse<Map<String, *>>().println()

        assert(collection["name"] == "Kotlin POM")
        assert(collection["repositories", "repository", "id"] == "central")
        assert(collection["dependencies", "dependency", 0, "artifactId"] == "junit-jupiter")
        assert(collection["dependencies", "dependency", 1, "artifactId"] == "kotlin-test")
        assert(collection["alien", "property"] == "val")
        assert(collection["alien", ""] == "text")
    }

    @Test
    @Disabled // TODO Fix this case
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

        val collection = xml.parse<Map<String, *>>().println()

        assertEquals("header", collection["bean", "id"])
        assertEquals(2, (collection["camelContext", "rest", "post"] as? List<*>)?.size)
        assertEquals(3, (collection["camelContext", "rest", "get"] as? List<*>)?.size)
    }

    @Test fun `XML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize()
        val deserializedPlayer = serializedPlayer.parse(Player::class)

        assert(player.name == deserializedPlayer.name)
        assert(player.number == deserializedPlayer.number)
        assert(player.category.start == deserializedPlayer.category.start)
        assert(player.category.endInclusive == deserializedPlayer.category.endInclusive)

        val players = Players(
            listOf(
                Player("Michael", 23, 18..65),
                Player("Magic", 32, 18..65),
            )
        )
        val serializedPlayers = players.serialize()
        val deserializedPlayers = serializedPlayers.parse(Players::class)

        val first = deserializedPlayers.players.first()
        val last = deserializedPlayers.players.last()
        assert(players.players.first().name == first.name)
        assert(players.players.first().number == first.number)
        assert(players.players.first().category.start == first.category.start)
        assert(players.players.first().category.endInclusive == first.category.endInclusive)

        assert(players.players.last().name == last.name)
        assert(players.players.last().number == last.number)
        assert(players.players.last().category.start == last.category.start)
        assert(players.players.last().category.endInclusive == last.category.endInclusive)
    }

    @Test fun `Parse invalid XML range` () {
        assertFailsWith<ParseException> {
            """
            <Player>
                <name>Michael</name>
                <number>23</number>
                <category>error</category>
            </Player>
            """
            .trimIndent()
            .parse(Player::class)
        }
    }

    @Test fun `Parse invalid XML range start` () {
        assertFailsWith<ParseException> {
            """
            <Player>
                <name>Michael</name>
                <number>23</number>
                <category>
                    <error>18</error>
                    <endInclusive>65</endInclusive>
                </category>
            </Player>
            """
            .trimIndent()
            .parse(Player::class)
        }
    }

    @Test fun `Parse invalid XML range end` () {
        assertFailsWith<ParseException> {
            """
            <Player>
                <name>Michael</name>
                <number>23</number>
                <category>
                    <start>18</start>
                    <error>65</error>
                </category>
            </Player>
            """
            .trimIndent()
            .parse(Player::class)
        }
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
        """.trimIndent().toStream().parseObjects<Map<String, *>>()
        assert(parse[0]["a"] == "b")
    }

    @Test fun `Serialize by content type` () {
        val result = mapOf("aKey" to 1, "bKey" to 2).serialize(Xml.contentType)
        assert(result.contains("aKey") && result.contains("bKey"))
    }

    @Test fun `Parse exceptions contains failed field`() {
        try {
            """
            <Device>
              <id>f</id>
              <brand>br</brand>
              <model>mo</model>
              <os>ANDROI</os>
              <osVersion>v</osVersion>
              <alias>al</alias>
            </Device>
            """.parse(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "com.hexagonkt.serialization.XmlTest\$Device[\"os\"]")
        }
    }

    @Test fun `Invalid format exceptions field is 'null'`() {
        try {
            """
            <ArrayList>
                <item>
                  <id>f</id>
                  <brand>br</brand>
                  <model>mo</model>
                  <os>ANDROI</os>
                  <osVersion>v</osVersion>
                  <alias>al</alias>
                </item>
            """.parse(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "")
        }
    }

    @Test fun `Parse an invalid class throws exception`() {
        try {
            """
            <ArrayList>
                <item>
                  <id>f</id>
                  <brand>br</brand>
                  <model>mo</model>
                  <os>ANDROI</os>
                  <osVersion>v</osVersion>
                  <alias>al</alias>
                </item>
            </ArrayList>
            """.parseObjects<Device>()

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            val fieldFullName = "com.hexagonkt.serialization.XmlTest\$Device[\"os\"]"
            assert(e.field == "java.util.ArrayList[0]->$fieldFullName")
        }
    }
}
