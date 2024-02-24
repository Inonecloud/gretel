package com.raw.gretel.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Iv(
    @Id
    val id:String,
    val iv:String
)