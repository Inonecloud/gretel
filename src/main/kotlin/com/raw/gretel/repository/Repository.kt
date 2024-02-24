package com.raw.gretel.repository

import com.raw.gretel.domain.Iv
import com.raw.gretel.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
  fun findByUsername(username:String):User?

}

interface IvRepository: MongoRepository<Iv, String>{

}