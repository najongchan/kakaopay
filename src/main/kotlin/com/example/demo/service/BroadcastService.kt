package com.example.demo.service

import com.example.demo.repository.BroadcastRepository
import com.example.demo.entity.Broadcast
import com.example.demo.entity.Response
import com.example.demo.exception.GloblaExceptionHandler
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
            throw GloblaExceptionHandler.BroadcastNotExist(
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
        // TODO: 2020/11/21 토큰생성 로직 
        return Random(3).toString()
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
            throw GloblaExceptionHandler.OwnerReceiveException(
                    "Owner Can't Receive Own Money"
            )
        }

        if (broadcast.received[request["user"]] != null) {
            throw GloblaExceptionHandler.UserAlreadyReceivedException(
                    "User Already Received Money"
            )
        }

        if (broadcast.room != request["room"]) {
            throw GloblaExceptionHandler.RoomMemberOnlyException(
                    "Only Room Member Can Request"
            )
        }

        if (broadcast.splits.size <= 0) {
            throw GloblaExceptionHandler.SplitsExhaustedException(
                    "Money Exhausted!"
            )
        }

        if (broadcast.splits_expired_at < LocalDateTime.now()) {
            throw GloblaExceptionHandler.SplitsExpiredException(
                    "Time Out!"
            )
        }

        return true
    }
//    fun postValidationCheck(request: Map<String, Int>)
//            : Boolean{
//        if (request["total_money"] == null) {
//            throw GloblaExceptionHandler.OwnerReceiveException(
//                    "Owner Can't Receive Own Money"
//            )
//        }
//
//        if (request["target"] == null) {
//            throw GloblaExceptionHandler.UserAlreadyReceivedException(
//                    "User Already Received Money"
//            )
//        }
//        return true
//    }

    fun getBroadcastFilter(token: String): Response{
        val broadcast = this.getByToken(token)

        if (broadcast.data_expired_at < LocalDateTime.now()) {
            throw GloblaExceptionHandler.BroadcastDataExpired(
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