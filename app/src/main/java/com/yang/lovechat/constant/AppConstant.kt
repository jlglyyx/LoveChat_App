package com.yang.lovechat.constant

import com.yang.lovechat.util.getCache


object AppConstant {

    object ClientInfo {


        const val TAG_LOG = "httpLog"


        //google 开关 上线改成 true
        const val OPEN_GOOGLE = false
        val BASE_IP = getCache(Constant.IP, "192.168.8.35")

        val BASE_WS_URL = "ws://${BASE_IP}:8000/chat"
        val BASE_URL = "http://${BASE_IP}:8000/"

        val BASE_FILE_URL = BASE_URL

        const val BASE_PRIVACY_POLICY_URL = "https://www.dawnmeet-chat.com/dawnmeet_privacy_policy.html"

        const val BASE_SERVICE_POLICY_URL = "https://www.dawnmeet-chat.com/dawnmeet_services.html"

        const val CONNECT_TIMEOUT = 60*1000L

        const val READ_TIMEOUT = 60*1000L

        const val WRITE_TIMEOUT = 60*1000L


    }

    object Constant {

        const val CLICK_TIME: Long = 1000

        var isShowBuy = false

        var ppvsEnable = false

        var threePay = false

        const val IS_ALLOW = "is_allow"


        const val PPV_BLUR_RADIUS = 50

        const val PPV_BLUR_VIDEO_RADIUS = 20


        var totalExpiredSecond = 30 * 60

        var picturePrice = 30

        var videoPrice = 60

        var connectPrice = 80

        const val LAST_MSG_ID = "lastMsgId"

        const val MESSAGE_ID = "messageId"

        const val MEDIA_IDS = "mediaIds"


        const val PAGE_NUM = "pageNum"

        const val PAGE_SIZE = "pageSize"

        const val MEDIAINFOS = "mediaInfos"

        const val MEDIAINFO_IDS = "mediaInfoIds"

        const val PAGE_SIZE_COUNT = 20

        var MEDIA_ENABLE_TIME = 1000 * 60 * 30 + 1000

        var LAST_OPEN_VIP_TIME = System.currentTimeMillis()

        var LAST_OPEN_VIP_COUNT = 0

        const val IS_LOGIN = "isLogin"

        const val TOKEN = "token"

        const val KEY = "key"

        const val INFO = "info"


        const val UPDATE = "update"

        const val HAS_SWIP = "hasSwip"

        const val TARGET_ID = "targetId"

        const val EXTRA_DATA = "extra_data"

        const val IS_CAN_SEND = "isCanSend"

        const val TOUCH_TYPE = "touchType"

        const val IS_THREE_PAY = "threePay"

        const val NEED_OTHER_PAY_TRY = "needOtherPayTry"


        const val PUSH_MARK = "pushMark"

        const val IS_OFFLINE = "is_offline"

        const val PUSH_EXTRA = "pushExtra"

        const val IS_NOTICE_INTO = "is_notice_into"

        const val IS_HAS_LOCATION = "is_has_location"

        const val IS_HAS_NOTICE = "is_has_notice"

        const val PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs8T0u6LsEXrnwozWeNxFpxV5cTUd6eCbiCDZhpgSHCCL3sKTWud3M+vWj/gx7Jh8Zq13raCQ8vKgzk+brx9BaXLd34AT4Xb8Cx9tha35UsbPR4OqsGbN7LWQB9Y/2JWsriJxUg06nZl9LjISgNg4h2TbwsfEkVNLLj/k4b6UIBMBI0GUYFHSsLFqMqTw54vR/tzH2Z6/O4N++LeuJLVI474bUZCKxTXAVeBPTovJSgTvw2tCKZ0vL5P4tsoEDb7ckbxpK0ALA+fUqk5raGLyaViouMVr3shXVFR4SWqWjCIq9PMEaFNIYuZ0vqv09CRrwxlr/5JTHJfgDhltmNfmMwIDAQAB"

        const val LOGIN_TYPE = "loginType"

        const val EMAIL = "Email"

        const val GOOGLE = "Google"

        const val IP = "ip"

        const val URL = "url"

        const val TITLE = "title"

        const val PAGE = "page"

        const val VIP_DATA = "vip_data"

        const val DATA = "data"

        const val MODEL_DATA = "model_data"

        const val PRIVILEGE_DATA = "privilege_data"

        const val HAS_SHOW_LIMIT = "has_show_limit"

        const val IS_ME = "isMe"

        const val IS_PRIVATE = "isPrivate"

        const val POSITION = "position"

        const val TYPE = "type"

        const val ID = "id"

        const val IS_TURN_ONS = "is_turn_ons"



        const val USER_ID = "userId"


        const val CONV_ID = "convId"

        const val FRIEND_AVATAR = "friend_avatar"

        const val FRIEND_NAME = "friend_name"

        const val FRIEND_USER_ID = "friend_user_id"


        const val MATCH = "MATCH"

        const val USER_INFO = "user_info"

        const val WANT = "want"

        const val INTEREST = "interest"

        const val PROFESSION = "profession"

        const val TURN = "turn"

        const val HOBBY_TAG = "hobby_tag"




        const val SOCIAL_AIM = "SocialAim"

        const val IS_SUBSCRIPTION = "is_subscription"

        const val PRODUCT_DIAMOND = 0

        const val PRODUCT_VIP = 1

        const val PRODUCT_CACHE = "product_cache"

        const val IS_VPN = "is_vpn"

        const val IS_REVIEW_VERSION = "reviewVersion"

        const val NOTICE_CLICK = "notice_click"

        const val NOTICE_TYPE = "notice_type"

        const val BIZ_ID = "bizId"

        const val LOGIN_NOTICE = "login_notice"

        const val HAS_MESSAGE_NOTICE = "has_message_notice"

        const val LOCATION_NOTICE = "location_notice"

        const val START_NOTICE = "start_notice"

        const val HAS_EXPOSURE = "has_exposure"

        const val PAY_FLASH_CHAT = "FlashChat"

        const val PAY_VIP = "Vip"

        const val PAY_PRIVATE_PHOTO = "PrivatePhoto"

        const val PAY_PRIVATE_VIDEO = "PrivateVideo"



    }

    object RIMConstant {

        const val RIM_TOKEN = "rim_token"

        const val APP_DEV_KEY = "25wehl3u243nw"
        //
        const val APP_TEST_KEY = "8luwapkv87ycl"

        const val APP_REAL_KEY = "8brlm7uf8y5g3"

        const val SYSTEM_NOTICE = "as-system"

        const val RC_TXT_MSG = "RC:TxtMsg"

        const val RC_IMG_MSG = "RC:ImgMsg"

        const val RC_IMG_VIDEO = "AS:VideoMsg"

        const val RC_NTF_MSG = "RC:InfoNtf"

        const val RC_CMD_MSG = "RC:CmdMsg"


        const val RC_PP_VM_MSG = "AS:PPVMsg"

        const val RC_TURN_ONS_MSG = "AS:TurnOns"

        const val RC_LIMIT_MESSAGE_MSG = "AS:LimitMessage"


        const val RC_SEND_TEXT_MSG = "Text"

        const val RC_SEND_PUBLIC_IMAGE_MSG = "Pic"

        const val RC_SEND_PRIVATE_IMAGE_S_MSG = "PPics"

        const val RC_SEND_PRIVATE_IMAGE_MSG = "PPic"

        const val RC_SEND_PRIVATE_VIDEO_MSG = "PVideo"


        const val RC_SEND_PRIVATE_VIDEO_S_MSG = "PVideos"

        const val CMD_MATCH_SUCCESS = "MatchSuccess"

        const val CMD_NEW_VISITOR = "NewVisitor"

        const val CMD_NEW_WHO_LIKE_ME = "NewWhoLikeMe"

        const val CMD_FLASH_CHAT = "FlashChat"
    }

    object EventConstant{

        const val EVENT_RE_LOGIN = "event_re_login"

        const val EVENT_REFRESH_CARD_LIST = "event_refresh_card_list"

        const val EVENT_SET_PAGE = " event_set_page"

        const val EVENT_SET_LIKE_PAGE = " event_set_like_page"


        const val EVENT_REFRESH_MY_USER_INFO = " event_refresh_my_user_info"

        const val EVENT_UPDATE_USER_INFO_NOW = "event_update_user_info_now"

        const val EVENT_UPDATE_CONVERSATION_READ_COUNT = "event_update_conversation_read_count"

        const val EVENT_UPDATE_ALL_CONVERSATION_READ_COUNT = "event_update_all_conversation_read_count"

        const val EVENT_SHIELD_CONVERSATION = "event_shield_conversation"

        const val EVENT_FAST_CONNECT_SUCCESS = "event_fast_connect_success"

        const val EVENT_FOREGROUND_CHANGE = "event_foreground_change"

        const val EVENT_RECEIVED_LIKE_I = "event_received_like_i"

        const val EVENT_RECEIVED_VIEW_I = "event_received_view_i"

        const val EVENT_BUY_VIP_SUCCESS = "event_buy_vip_success"

    }




    object NoticeChannel{

        const val MESSAGE_CHANNEL_ID = "message_channel_id"

        const val MESSAGE_NOTICE = "MessageNotice"

    }
}