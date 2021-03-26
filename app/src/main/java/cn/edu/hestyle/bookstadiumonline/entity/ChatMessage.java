package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    /** ChatMessage content的最大长度 */
    public static final Integer CHAT_MESSAGE_CONTENT_MAX_LENGTH = 500;

    /** id */
    private Integer id;
    /** chatId */
    private Integer chatId;
    /** chatType */
    private Integer chatType;
    /** fromAccountId */
    private Integer fromAccountId;
    /** toAccountId */
    private Integer toAccountId;
    /** content */
    private String content;
    /** 发送时间 */
    private Date sentTime;
    /** 是否删除，0未删除，1已删除 */
    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Integer getChatType() {
        return chatType;
    }

    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Integer fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", chatType=" + chatType +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", content='" + content + '\'' +
                ", sentTime=" + sentTime +
                ", isDelete=" + isDelete +
                '}';
    }
}
