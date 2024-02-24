package com.raw.gretel.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id
    val userId: String,
    val username: String,
    val chatId: Long,
    var status: UserState,
) {
    var groups: MutableSet<String> = mutableSetOf()
    constructor(userId: String,username: String, chatId: Long, status: UserState, groups:MutableSet<String> ) : this(
        userId, username, chatId, status
    ){
        this.groups = groups
    }
}
