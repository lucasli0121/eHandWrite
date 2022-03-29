package com.libwriting.utils

import java.util.*

class UUIDTool {
    companion object {
        fun getId(): String {
            var uuid = UUID.randomUUID()
            return uuid.toString().replace("-", "")
        }
    }
}