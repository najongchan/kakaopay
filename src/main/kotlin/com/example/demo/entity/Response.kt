package com.example.demo.entity

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class Response (
        val created_at: String,
        val total_money: Int,
        val used_money: Int,
        val received: Map<Int, String>
){
}