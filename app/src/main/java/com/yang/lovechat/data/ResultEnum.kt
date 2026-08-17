package com.yang.lovechat.data

enum class ResultEnum(val code: Int, val message: String, val success: Boolean) {

    REQUEST_FAILED_ERROR(2001, "Request failed", false),

    REQUEST_SUCCESS(200, "Request success", true),


    LOGIN_EXPIRED_ERROR(401, "Login expired, please log in again", false),
    USER_NOT_EXIST_ERROR(401, "User does not exist", false),

    NEED_VIP_PERMISSION_ERROR(2002, "VIP membership required", false),
    NEED_DIAMOND_ERROR(2003, "Diamonds required to unlock", false),
    ORDER_ABNORMAL_ERROR(2004, "Order exception", false),
    FILE_NO_SUPPORT_ERROR(2005, "Unsupported file type", false),
    CONVERSATION_EXIST_ERROR(2006, "Conversation already exists", false),
    CONVERSATION_NOT_FOUND_ERROR(2007, "Conversation does not exist", false),
    CONVERSATION_BLOCKED_ERROR(2008, "Conversation blocked", false),
    MSG_FORMAT_UNSUPPORTED_ERROR(2009, "Unsupported message format", false),
    SEND_MESSAGE_FAILED_ERROR(2010, "Failed to send message", false),
    FREQUENT_OPERATION_ERROR(2011, "Operation too frequent, please try again later", false),
    PAYMENT_VOUCHER_USED_ERROR(2012, "Payment voucher is occupied by another order", false),
    WALLET_RECORD_NOT_FOUND_ERROR(2013, "Wallet record not found", false),
    WALLET_UPDATE_FAILED_ERROR(2014, "Failed to update wallet balance", false),
    PASSWORD_WRONG_ERROR(2015, "Password is incorrect", false),
    MSG_TYPE_UNSUPPORTED_ERROR(2016, "Unsupported message type", false),
    MARK_READ_FAILED_ERROR(2017, "Failed to mark as read", false),
    COMMAND_FORMAT_UNSUPPORTED_ERROR(2018, "Unsupported command format", false),

    PARAM_INVALID_ERROR(3000, "Invalid request parameters", false),
    SYSTEM_UNKNOWN_ERROR(3001, "System internal exception", false);

    companion object {
        fun getByCode(code: Int): ResultEnum? {
            return ResultEnum.entries.find { it.code == code }
        }
    }
}