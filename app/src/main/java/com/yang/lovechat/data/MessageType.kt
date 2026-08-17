package com.yang.lovechat.data

enum class MessageType(val type: Int) {

    TEXT(0),

    IMAGE_VIDEO(1),

    SYSTEM(2);


}

enum class InstructionType(val code: Int,val value: String) {

    PING(100,"PING"),

    PONG(101,"PONG"),

    DISLIKE(0,"DISLIKE"),

    LIKE(1,"LIKE"),

    CONNECT(2,"CONNECT"),

    VIEWED(3,"VIEWED"),

    MATCH(4,"MATCH"),

    FAST_CONNECT(5,"FAST_CONNECT"),

    LIKE_I(6,"LIKE_I"),

    VIEW_I(7,"VIEW_I"),




    READ_CONVERSATION(30,"READ_CONVERSATION");
}