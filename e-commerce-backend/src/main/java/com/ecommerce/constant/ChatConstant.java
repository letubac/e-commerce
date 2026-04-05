package com.ecommerce.constant;

/**
 * Chat Module Constants
 * Error codes: E500-E549
 * Success codes: S500-S529
 */
/**
 * author: LeTuBac
 */
public class ChatConstant {

    // ============ ERROR CODES ============

    // Conversation errors (E500-E509)
    public static final String E500_CONVERSATION_NOT_FOUND = "500_CONVERSATION_NOT_FOUND";
    public static final String E501_CONVERSATION_FETCH_FAILED = "501_CONVERSATION_FETCH_FAILED";
    public static final String E502_CONVERSATION_UPDATE_ERROR = "502_CONVERSATION_UPDATE_ERROR";
    public static final String E503_CONVERSATION_CREATION_FAILED = "503_CONVERSATION_CREATION_FAILED";
    public static final String E504_CONVERSATION_STATUS_UPDATE_FAILED = "504_CONVERSATION_STATUS_UPDATE_FAILED";
    public static final String E505_CONVERSATION_ASSIGNMENT_FAILED = "505_CONVERSATION_ASSIGNMENT_FAILED";
    public static final String E506_CONVERSATION_ALREADY_CLOSED = "506_CONVERSATION_ALREADY_CLOSED";
    public static final String E507_CONVERSATION_ASSIGN_ERROR = "507_CONVERSATION_ASSIGN_ERROR";

    // Message errors (E510-E519)
    public static final String E510_MESSAGE_NOT_FOUND = "510_MESSAGE_NOT_FOUND";
    public static final String E511_MESSAGE_SEND_ERROR = "511_MESSAGE_SEND_ERROR";
    public static final String E512_MESSAGE_FETCH_ERROR = "512_MESSAGE_FETCH_ERROR";
    public static final String E513_MESSAGE_DELETE_ERROR = "513_MESSAGE_DELETE_ERROR";
    public static final String E514_MARK_READ_ERROR = "514_MARK_READ_ERROR";
    public static final String E515_UNREAD_COUNT_ERROR = "515_UNREAD_COUNT_ERROR";
    public static final String E516_MESSAGE_CONTENT_TOO_LONG = "516_MESSAGE_CONTENT_TOO_LONG";

    // Access/Permission errors (E520-E529)
    public static final String E520_USER_NOT_CONVERSATION_OWNER = "520_USER_NOT_CONVERSATION_OWNER";
    public static final String E521_USER_NOT_FOUND = "521_USER_NOT_FOUND";
    public static final String E522_ADMIN_NOT_FOUND = "522_ADMIN_NOT_FOUND";
    public static final String E523_CONVERSATION_NOT_ASSIGNED = "523_CONVERSATION_NOT_ASSIGNED";

    // Validation errors (E530-E539)
    public static final String E530_INVALID_USER_ID = "530_INVALID_USER_ID";
    public static final String E531_INVALID_SUBJECT = "531_INVALID_SUBJECT";
    public static final String E532_INVALID_MESSAGE_CONTENT = "532_INVALID_MESSAGE_CONTENT";
    public static final String E533_INVALID_USERNAME = "533_INVALID_USERNAME";
    public static final String E534_INVALID_CONVERSATION_ID = "534_INVALID_CONVERSATION_ID";
    public static final String E535_INVALID_STATUS = "535_INVALID_STATUS";
    public static final String E536_INVALID_ADMIN_ID = "536_INVALID_ADMIN_ID";
    public static final String E537_OWNERSHIP_CHECK_FAILED = "537_OWNERSHIP_CHECK_FAILED";
    public static final String E538_INVALID_CONVERSATION_DATA = "538_INVALID_CONVERSATION_DATA";

    // File upload errors (E540-E544)
    public static final String E540_FILE_UPLOAD_ERROR = "540_FILE_UPLOAD_ERROR";
    public static final String E541_FILE_TOO_LARGE = "541_FILE_TOO_LARGE";
    public static final String E542_FILE_TYPE_NOT_ALLOWED = "542_FILE_TYPE_NOT_ALLOWED";
    public static final String E543_FILE_EMPTY = "543_FILE_EMPTY";

    // General errors (E545-E549)
    public static final String E545_CHAT_OPERATION_FAILED = "545_CHAT_OPERATION_FAILED";
    public static final String E546_UNREAD_COUNT_ERROR = "546_UNREAD_COUNT_ERROR";

    // ============ SUCCESS CODES ============

    // Conversation operations (S500-S509)
    public static final String S500_CONVERSATION_CREATED = "S500_CONVERSATION_CREATED";
    public static final String S501_CONVERSATION_RETRIEVED = "S501_CONVERSATION_RETRIEVED";
    public static final String S502_CONVERSATIONS_LISTED = "S502_CONVERSATIONS_LISTED";
    public static final String S503_CONVERSATION_UPDATED = "S503_CONVERSATION_UPDATED";
    public static final String S504_CONVERSATION_CLOSED = "S504_CONVERSATION_CLOSED";
    public static final String S505_CONVERSATION_REOPENED = "S505_CONVERSATION_REOPENED";
    public static final String S506_CONVERSATION_ASSIGNED = "S506_CONVERSATION_ASSIGNED";
    public static final String S507_UNASSIGNED_CONVERSATIONS_LISTED = "S507_UNASSIGNED_CONVERSATIONS_LISTED";

    // Message operations (S510-S519)
    public static final String S510_MESSAGE_SENT = "S510_MESSAGE_SENT";
    public static final String S511_MESSAGE_RETRIEVED = "S511_MESSAGE_RETRIEVED";
    public static final String S512_MESSAGES_LISTED = "S512_MESSAGES_LISTED";
    public static final String S513_MESSAGE_DELETED = "S513_MESSAGE_DELETED";
    public static final String S514_MESSAGES_MARKED_READ = "S514_MESSAGES_MARKED_READ";

    // File operations (S520-S524)
    public static final String S520_FILE_UPLOADED = "S520_FILE_UPLOADED";

    // Other operations (S525-S529)
    public static final String S525_UNREAD_COUNT_RETRIEVED = "S525_UNREAD_COUNT_RETRIEVED";

    // Sender types
    public static final String SENDER_TYPE_USER = "USER";
    public static final String SENDER_TYPE_ADMIN = "ADMIN";
    public static final String SENDER_TYPE_AI = "AI";

    // Conversation statuses
    public static final String STATUS_OPEN = "OPEN";

    // Private constructor to prevent instantiation
    private ChatConstant() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }
}
