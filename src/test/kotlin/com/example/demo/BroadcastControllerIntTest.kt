package com.example.demo

import com.example.demo.entity.Broadcast
import com.example.demo.entity.Response
import com.example.demo.service.BroadcastService
import net.minidev.json.JSONObject
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.random.Random


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BroadcastControllerIntTest @Autowired constructor(
        private val broadcastService: BroadcastService,
        private val restTemplate: TestRestTemplate
) {
    private val defaultBroadcastId = ObjectId.get()
    private val defaultToken = Random(3).toString()
    private val defaultOwner = "najong"
    private val defaultRoom = "2020"

    @LocalServerPort
    protected var port: Int = 0

    @BeforeEach
    fun setUp() {
        broadcastService.deleteAll()
    }

    private fun getRootUrl(): String? = "http://localhost:$port/broadcast"

    private fun saveOneBroadcast() {
            val splits = broadcastService.makeSplits(60000, 3)
            broadcastService.save(Broadcast(
                    token = defaultToken,
                    owner = defaultOwner,   // 뿌린사람
                    room = defaultRoom,    // 뿌린 방
                    targets = 3, // 받기 대상 수
                    total_money = 60000, // 뿌린 금액
                    used_money = 0, // 총 받은 금액
                    received = hashMapOf<String, Int>(), // 받기 정보
                    splits = splits,  // 분배
            ))
    }

    @Test
    fun `should return single broadcast by token`() {
        val headers = HttpHeaders()
        headers.set("X-ROOM-ID", "2020")
        headers.set("X-USER-ID", "kotlin-test-client")
//        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.GET,
                HttpEntity("", headers),
                Response::class.java
        )
        println("ggggggg")
        println(response.statusCode.value())
        println(response.body)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `should return broadcast token`() {
        val headers = HttpHeaders()
        headers.set("X-ROOM-ID", "2020")
        headers.set("X-USER-ID", "kotlin-test-client")
        headers.set("Content-Type", "application/json")

        val body= JSONObject()
        body["total_money"] = 30000
        body["targets"] = 4

        val response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.POST,
                HttpEntity(body, headers),
                String::class.java
        )
        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `should receive broadcast money by token`() {
        saveOneBroadcast()

        val headers = HttpHeaders()
        headers.set("X-ROOM-ID", defaultRoom)
        headers.set("X-USER-ID", "numinu")
        headers.set("Content-Type", "application/json")

//        println(defaultToken)
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                String::class.java
        )
//        println(response)
//
//        val headers1 = HttpHeaders()
//        headers1.set("X-ROOM-ID", defaultRoom)
//        headers1.set("X-USER-ID", "fdfd")
//        headers1.set("Content-Type", "application/json")
//
//        println(defaultToken)
//        val response1 = restTemplate.exchange(
//                getRootUrl() + "/$defaultToken",
//                HttpMethod.PUT,
//                HttpEntity("", headers1),
//                String::class.java
//        )
//        println(response1)
//
//        val headers2 = HttpHeaders()
//        headers2.set("X-ROOM-ID", defaultRoom)
//        headers2.set("X-USER-ID", "najj")
//        headers2.set("Content-Type", "application/json")
//
//        println(defaultToken)
//        val response2 = restTemplate.exchange(
//                getRootUrl() + "/$defaultToken",
//                HttpMethod.PUT,
//                HttpEntity("", headers2),
//                String::class.java
//        )
//        println(response2)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)

        @Test
        fun `should throw error token not exist`() {

        }
    }
}