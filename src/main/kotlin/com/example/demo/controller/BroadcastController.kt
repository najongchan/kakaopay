package com.example.demo.controller

import com.example.demo.entity.Broadcast
import com.example.demo.entity.BroadcastRequest
import com.example.demo.entity.Response
import com.example.demo.service.BroadcastService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
@RequestMapping("/broadcast", produces = arrayOf("application/json"))
class BroadcastController(
        private val broadcastService: BroadcastService
) {
    @GetMapping("/{token}")
    fun getOneBroadCast(@PathVariable("token") token: String):
            ResponseEntity<Response> {
        println(token)
        val response = broadcastService.getBroadcastFilter(token)
        println(response)
        return ResponseEntity.ok(response)
    }

    @PostMapping("")
    fun createBroadcast(
            @RequestHeader(value = "X-USER-ID", required = true) user: String,
            @RequestHeader(value = "X-ROOM-ID", required = true) room: String,
            @RequestBody body: BroadcastRequest
    ): ResponseEntity<String> {
//        val request = mapOf("total_money" to body.total_money, "targets" to body.targets)
//        if (!broadcastService.postValidationCheck(request = request)) {
//            throw Exception("Anyway Exception")
//        }

        val nums = broadcastService.makeSplits(body.total_money, body.targets)
        val token = broadcastService.makeToken()
        broadcastService.save(Broadcast(
                token = token,
                owner = user,   // 뿌린사람
                room = room,    // 뿌린 방
                targets = body.targets, // 받기 대상 수
                total_money = body.total_money, // 뿌린 금액
                used_money = 0, // 총 받은 금액
                received = hashMapOf<String, Int>(), // 받기 정보
                splits = nums,  // 분배
        ))
        return ResponseEntity.ok(token)
    }

    @PutMapping("/{token}")
    fun receiveBroadcast(
            @RequestHeader(value = "X-USER-ID", required = true) user: String,
            @RequestHeader(value = "X-ROOM-ID", required = true) room: String,
            @PathVariable("token") token: String
    ): ResponseEntity<String> {
        val broadcast = broadcastService.getByToken(token)
        val request = mapOf("user" to user, "room" to room)

        // validation check
        if (!broadcastService.putValidationCheck(broadcast, request)) {
            throw Exception("Anyway Exception")
        }

        val receiveAmount = broadcastService.receiveMoney(broadcast, request)
        return ResponseEntity.ok().body(receiveAmount.toString())
    }

}