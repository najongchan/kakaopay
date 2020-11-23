package com.example.demo.entity

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import kotlin.random.Random

@Document(collection = "broadcast")
data class Broadcast (
        @Id @JsonSerialize(using = ToStringSerializer::class)
        val id: ObjectId = ObjectId.get(),  // 뿌리기 구분
        val token: String,   // 뿌리기 구분 요청값
        val owner: String,  // 뿌린사람
        val room: String,   // 뿌린 대화방

        val targets: Int,   // 대상 수
        val total_money: Int,   // 뿌린금액
        var used_money: Int,    // 받은금액
        val received: HashMap<String, Int>,    // 받은 사람, 금액 리스트
        val splits: MutableList<Int>,

        val created_at: LocalDateTime = LocalDateTime.now(),
        val splits_expired_at: LocalDateTime = created_at.plusMinutes(10),
        val data_expired_at: LocalDateTime = created_at.plusDays(7)
) {
}