package com.example.demo.service

import com.example.demo.repository.BroadcastRepository
import com.example.demo.entity.Broadcast
import com.example.demo.entity.Response
import com.example.demo.exception.GlobalExceptionHandler
import org.apache.commons.lang3.RandomStringUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Service
class BroadcastService (
        private val broadcastRepository: BroadcastRepository
) {
    fun getById(id : String) : Broadcast {
        return broadcastRepository.findOneById(ObjectId(id))
    }

    fun getByToken(token: String) : Broadcast {
        try {
            return broadcastRepository.findOneByToken(token)
        } catch (e: Exception) {
            throw GlobalExceptionHandler.BroadcastNotExist(
                    "Broadcast doesn't exist"
            )
        }

    }

    fun deleteAll() {
        broadcastRepository.deleteAll()
    }

    fun save(broadcast: Broadcast) {
        broadcastRepository.save(broadcast)
    }

    fun makeToken(): String {
        return RandomStringUtils.randomAlphanumeric(3)
    }

    fun makeSplits(total_money: Int, targets: Int): MutableList<Int>{
        val nums = mutableListOf<Int>()
        var total = total_money

        for (i in 0 until targets - 1) {
            nums.add(Random.nextInt(0, total))
            total -= nums[i]
        }
        nums.add(total)

        return nums
    }

    fun receiveMoney(broadcast: Broadcast, request: Map<String, String>): Int{
        val receiveAmount = broadcast.splits[0]
        val user = request["user"].toString()

        broadcast.splits.removeFirst()
        broadcast.received[user] = receiveAmount
        broadcast.used_money += receiveAmount
        this.save(broadcast)

        return receiveAmount
    }

    fun putValidationCheck(broadcast: Broadcast, request: Map<String, String>)
            : Boolean{
        if (broadcast.owner == request["user"]) {
            throw GlobalExceptionHandler.OwnerReceiveException(
                    "Owner Can't Receive Own Money"
            )
        }

        if (broadcast.received[request["user"]] != null) {
            throw GlobalExceptionHandler.UserAlreadyReceivedException(
                    "User Already Received Money"
            )
        }

        if (broadcast.room != request["room"]) {
            throw GlobalExceptionHandler.RoomMemberOnlyException(
                    "Only Room Member Can Request"
            )
        }

        if (broadcast.splits.size <= 0) {
            throw GlobalExceptionHandler.SplitsExhaustedException(
                    "Money Exhausted!"
            )
        }

        if (broadcast.splits_expired_at < LocalDateTime.now()) {
            throw GlobalExceptionHandler.SplitsExpiredException(
                    "Time Out!"
            )
        }

        return true
    }

    fun getBroadcastFilter(token: String, user: String): Response{
        val broadcast = this.getByToken(token)

        if (broadcast.owner != user) {
            throw GlobalExceptionHandler.UnAuthorizedUser(
                    "UnAuthorized user request"
            )
        }

        if (broadcast.data_expired_at < LocalDateTime.now()) {
            throw GlobalExceptionHandler.BroadcastDataExpired(
                    "Token Expired!"
            )
        }

        val response = Response(
                broadcast.created_at.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                broadcast.total_money,
                broadcast.used_money,
                broadcast.received.entries.associateBy({ it.value }) { it.key }
        )

        return response
    }
}