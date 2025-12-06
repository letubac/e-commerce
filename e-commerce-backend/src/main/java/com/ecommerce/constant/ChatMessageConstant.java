package com.ecommerce.constant;

/**
 * Chat Message Code Constants
 * 
 * @author System
 * @version 1.0
 */
public class ChatMessageConstant {

    // Chat Conversation Codes (500-519)
    public static final String E500_CONVERSATION_NOT_FOUND = "500_CONVERSATION_NOT_FOUND";
    public static final String E501_CONVERSATION_CREATE_ERROR = "501_CONVERSATION_CREATE_ERROR";
    public static final String E502_CONVERSATION_ACCESS_DENIED = "502_CONVERSATION_ACCESS_DENIED";
    public static final String E503_CONVERSATION_LIST_ERROR = "503_CONVERSATION_LIST_ERROR";
    public static final String E504_CONVERSATION_UPDATE_ERROR = "504_CONVERSATION_UPDATE_ERROR";
    public static final String E505_CONVERSATION_ASSIGN_ERROR = "505_CONVERSATION_ASSIGN_ERROR";

    // Chat Message Codes (520-539)
    public static final String E520_MESSAGE_NOT_FOUND = "520_MESSAGE_NOT_FOUND";
    public static final String E521_MESSAGE_SEND_ERROR = "521_MESSAGE_SEND_ERROR";
    public static final String E522_MESSAGE_LIST_ERROR = "522_MESSAGE_LIST_ERROR";
    public static final String E523_MESSAGE_MARK_READ_ERROR = "523_MESSAGE_MARK_READ_ERROR";
    public static final String E524_MESSAGE_INVALID_TYPE = "524_MESSAGE_INVALID_TYPE";
    public static final String E525_MESSAGE_FILE_UPLOAD_ERROR = "525_MESSAGE_FILE_UPLOAD_ERROR";
    public static final String E526_MESSAGE_FILE_SIZE_EXCEEDED = "526_MESSAGE_FILE_SIZE_EXCEEDED";
    public static final String E527_MESSAGE_FILE_TYPE_INVALID = "527_MESSAGE_FILE_TYPE_INVALID";

    // Chat WebSocket Codes (540-549)
    public static final String E540_WEBSOCKET_CONNECTION_ERROR = "540_WEBSOCKET_CONNECTION_ERROR";
    public static final String E541_WEBSOCKET_AUTH_ERROR = "541_WEBSOCKET_AUTH_ERROR";
    public static final String E542_WEBSOCKET_SEND_ERROR = "542_WEBSOCKET_SEND_ERROR";

    // Success Codes
    public static final String S500_CONVERSATION_CREATED = "S500_CONVERSATION_CREATED";
    public static final String S501_CONVERSATION_UPDATED = "S501_CONVERSATION_UPDATED";
    public static final String S502_CONVERSATION_CLOSED = "S502_CONVERSATION_CLOSED";
    public static final String S503_CONVERSATION_ASSIGNED = "S503_CONVERSATION_ASSIGNED";
    public static final String S504_CONVERSATION_LIST_SUCCESS = "S504_CONVERSATION_LIST_SUCCESS";

    public static final String S520_MESSAGE_SENT = "S520_MESSAGE_SENT";
    public static final String S521_MESSAGE_LIST_SUCCESS = "S521_MESSAGE_LIST_SUCCESS";
    public static final String S522_MESSAGE_MARKED_READ = "S522_MESSAGE_MARKED_READ";
    public static final String S523_FILE_UPLOADED = "S523_FILE_UPLOADED";
}
