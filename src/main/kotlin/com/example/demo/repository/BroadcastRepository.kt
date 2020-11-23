package com.example.demo.repository

import com.example.demo.entity.Broadcast
//import org.bson.types.ObjectId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

//import org.springframework.data.mongodb.repository.MongoRepository

//interface BroadcastRepository: MongoRepository<Broadcast, ObjectId> {
//    fun findOneById(id: ObjectId): Broadcast
//    fun findOneByToken(token: String): Broadcast
//    override fun deleteAll()
//
//}

@Repository
interface BroadcastRepository: JpaRepository<Broadcast, String> {
    fun findOneByToken(token: String): Broadcast
    override fun deleteAll()
}