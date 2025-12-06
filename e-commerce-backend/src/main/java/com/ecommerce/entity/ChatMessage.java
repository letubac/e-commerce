package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.CHAT_MESSAGES)
public class ChatMessage {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.CHAT_MESSAGES)
    @Column(name = "id")
    private Long id;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_type")
    private String senderType; // USER, ADMIN

    @Column(name = "content")
    private String content;

    @Column(name = "message_type")
    private String messageType; // TEXT, IMAGE, FILE

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private Date readAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isFromUser() {
        return "USER".equals(senderType);
    }

    public boolean isFromAdmin() {
        return "ADMIN".equals(senderType);
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = new Date();
    }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }
}
