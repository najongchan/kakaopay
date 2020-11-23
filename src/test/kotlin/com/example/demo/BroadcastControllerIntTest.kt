package com.example.demo

import com.example.demo.entity.Broadcast
import com.example.demo.entity.Response
import com.example.demo.exception.GlobalExceptionHandler
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
    private val defaultToken = broadcastService.makeToken()
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

    private fun makeExpireBroadcastData() {
        val broadcast = broadcastService.getByToken(defaultToken)
        broadcast.created_at = broadcast.created_at.minusDays(7)
        broadcast.splits_expired_at = broadcast.created_at.minusMinutes(10)
        broadcast.data_expired_at = broadcast.data_expired_at.minusDays(7)
        broadcastService.save(broadcast)
    }

    private fun makeExpireBroadcastSplits() {
        val broadcast = broadcastService.getByToken(defaultToken)
        broadcast.created_at = broadcast.created_at.minusMinutes(10)
        broadcast.splits_expired_at = broadcast.created_at.minusMinutes(10)
        broadcastService.save(broadcast)
    }

    @Test
    fun `token not exist`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", defaultOwner)
        headers.set("Content-Type", "application/json")

        val response = restTemplate.exchange(
                getRootUrl() + "/tem",
                HttpMethod.GET,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(404, response.statusCode.value())
    }

    @Test
    fun `user id should not null`() {
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.GET,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(400, response.statusCode.value())
    }

    @Test
    fun `unauthorized user request`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", "notOwner")
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.GET,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(403, response.statusCode.value())
    }

    @Test
    fun `should return single broadcast by token`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", defaultOwner)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.GET,
                HttpEntity("", headers),
                Response::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `should not return expired broadcast data`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", defaultOwner)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        makeExpireBroadcastData()

        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.GET,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(404, response.statusCode.value())
    }

    @Test
    fun `should return created broadcast token`() {
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
        headers.set("X-USER-ID", "user1")
        headers.set("Content-Type", "application/json")

        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                String::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
    }

    @Test
    fun `should not receive own money`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", defaultOwner)
        headers.set("X-ROOM-ID", defaultRoom)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(403, response.statusCode.value())
    }

    @Test
    fun `should not receive more than twice`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", "user1")
        headers.set("X-ROOM-ID", defaultRoom)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val broadcast = broadcastService.getByToken(defaultToken)
        val request = mapOf<String, String>("user" to "user1")
        broadcastService.receiveMoney(broadcast, request)
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(403, response.statusCode.value())
    }

    @Test
    fun `should be in same room`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", "user1")
        headers.set("X-ROOM-ID", "different room")
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(403, response.statusCode.value())
    }

    @Test
    fun `money sold out`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", "user4")
        headers.set("X-ROOM-ID", defaultRoom)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        val broadcast = broadcastService.getByToken(defaultToken)
        val request1 = mapOf<String, String>("user" to "user1")
        val request2 = mapOf<String, String>("user" to "user2")
        val request3 = mapOf<String, String>("user" to "user3")

        broadcastService.receiveMoney(broadcast, request1)
        broadcastService.receiveMoney(broadcast, request2)
        broadcastService.receiveMoney(broadcast, request3)

        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(400, response.statusCode.value())
    }

    @Test
    fun `money times up`() {
        val headers = HttpHeaders()
        headers.set("X-USER-ID", "user1")
        headers.set("X-ROOM-ID", defaultRoom)
        headers.set("Content-Type", "application/json")

        saveOneBroadcast()
        makeExpireBroadcastSplits()

        val response = restTemplate.exchange(
                getRootUrl() + "/$defaultToken",
                HttpMethod.PUT,
                HttpEntity("", headers),
                GlobalExceptionHandler::class.java
        )

        assertEquals(400, response.statusCode.value())
    }

}