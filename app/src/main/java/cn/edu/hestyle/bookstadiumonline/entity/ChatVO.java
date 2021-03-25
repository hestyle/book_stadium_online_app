package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class ChatVO implements Serializable {
    /** id */
    private Integer id;
    /** chatType */
    private Integer chatType;
    /** fromAccountId */
    private Integer fromAccountId;
    /** fromAccountUsername */
    private String fromAccountUsername;
    /** fromAccountAvatarPath */
    private String fromAccountAvatarPath;
    /** toAccountId */
    private Integer toAccountId;
    /** toAccountUsername */
    private String toAccountUsername;
    /** toAccountAvatarPath */
    private String toAccountAvatarPath;
    /** fromUnreadCount */
    private Integer fromUnreadCount;
    /** toUnreadCount */
    private Integer toUnreadCount;
    /** lastChatMessage */
    private ChatMessage lastChatMessage;
    /** 创建时间 */
    private Date createdTime;
    /** 修改时间 */
    private Date modifiedTime;
    /** 是否删除，0未删除，1已删除 */
    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFromAccountUsername() {
        return fromAccountUsername;
    }

    public void setFromAccountUsername(String fromAccountUsername) {
        this.fromAccountUsername = fromAccountUsername;
    }

    public String getFromAccountAvatarPath() {
        return fromAccountAvatarPath;
    }

    public void setFromAccountAvatarPath(String fromAccountAvatarPath) {
        this.fromAccountAvatarPath = fromAccountAvatarPath;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getToAccountUsername() {
        return toAccountUsername;
    }

    public void setToAccountUsername(String toAccountUsername) {
        this.toAccountUsername = toAccountUsername;
    }

    public String getToAccountAvatarPath() {
        return toAccountAvatarPath;
    }

    public void setToAccountAvatarPath(String toAccountAvatarPath) {
        this.toAccountAvatarPath = toAccountAvatarPath;
    }

    public Integer getFromUnreadCount() {
        return fromUnreadCount;
    }

    public void setFromUnreadCount(Integer fromUnreadCount) {
        this.fromUnreadCount = fromUnreadCount;
    }

    public Integer getToUnreadCount() {
        return toUnreadCount;
    }

    public void setToUnreadCount(Integer toUnreadCount) {
        this.toUnreadCount = toUnreadCount;
    }

    public ChatMessage getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(ChatMessage lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "ChatVO{" +
                "id=" + id +
                ", chatType=" + chatType +
                ", fromAccountId=" + fromAccountId +
                ", fromAccountUsername='" + fromAccountUsername + '\'' +
                ", fromAccountAvatarPath='" + fromAccountAvatarPath + '\'' +
                ", toAccountId=" + toAccountId +
                ", toAccountUsername='" + toAccountUsername + '\'' +
                ", toAccountAvatarPath='" + toAccountAvatarPath + '\'' +
                ", fromUnreadCount=" + fromUnreadCount +
                ", toUnreadCount=" + toUnreadCount +
                ", lastChatMessage=" + lastChatMessage +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                ", isDelete=" + isDelete +
                '}';
    }
}
