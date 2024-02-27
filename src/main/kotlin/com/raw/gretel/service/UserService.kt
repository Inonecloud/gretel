package com.raw.gretel.service

import com.raw.gretel.domain.User
import com.raw.gretel.domain.UserState
import com.raw.gretel.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(
    val userRepository: UserRepository,
    val encryptionService: EncryptionService
) {

    fun saveUser(userId: Long, username: String, chatId: Long) {
        val maybeUser = userRepository.findById(userId.toString())
        if (maybeUser.isPresent) {
            return
        }
        val encryptedId = encryptionService.save(userId.toString())
        val user = User(encryptedId.id, username, chatId, UserState.ACTIVE)
        userRepository.save(user)
    }

    fun addGroupToUser(userId: Long, username: String, chatId: Long) {
        val encryptedId = encryptionService.findIv(userId.toString())

        val user = userRepository.findById(encryptedId.id)
        user.ifPresent { user ->
            user.groups.add(chatId.toString())
            userRepository.save(user)
        }
    }

    fun changeStatusUser(username: String, state: UserState) {
        userRepository.findByUsername(username)?.apply {
            status = state
            userRepository.save(this)
        }
    }

    fun getUser(username: String, groupId: Long): User? {
        return userRepository.findByUsername(username)?.takeIf { user ->
            user.groups.contains(groupId.toString())
        }
    }

    fun getUserById(userId: Long): User? {
        val encryptedId = encryptionService.findIv(userId.toString()).id
        return userRepository.findById(encryptedId)
            .orElse(null)
    }

    fun getUserByUsername(username: String): User? {
        val user = userRepository.findByUsername(username) ?: return null

        val iv = user.userId?.let { encryptionService.getIv(it) }
        if (iv != null) {
            val decryptedUserId = encryptionService.decrypt(user.userId, iv)
            return User(decryptedUserId, user.username, user.chatId, user.status, user.groups)
        }
        return null
    }

    fun delete(userId: Long): Long {
        val encryptedId = encryptionService.findIv(userId.toString())
        val user = userRepository.findById(encryptedId.id)
        if (user.isPresent) {
            val u = user.get()
            userRepository.delete(u)
            return u.chatId
        }
        return 0
    }


}