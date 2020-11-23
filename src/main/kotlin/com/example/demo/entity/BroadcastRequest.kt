package com.example.demo.entity

import org.jetbrains.annotations.NotNull

class BroadcastRequest(
        @field:NotNull
        val targets: Int,

        @field:NotNull
        val total_money: Int
)