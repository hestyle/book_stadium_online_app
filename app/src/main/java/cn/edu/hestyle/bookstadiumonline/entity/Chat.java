package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class Chat implements Serializable {
    /** chat类型 */
    public static final Integer CHAT_TYPE_USER_TO_USER = 0;
    public static final Integer CHAT_TYPE_USER_TO_MANAGER = 1;
    public static final Integer CHAT_TYPE_MANAGER_TO_USER = 2;

    /** id */
    private Integer id;
    /** chatType */
    private Integer chatType;
    /** fromAccountId */
    private Integer fromAccountId;
    /** toAccountId */
    private Integer toAccountId;
    /** fromUnreadCount */
    private Integer fromUnreadCount;
    /** toUnreadCount */
    private Integer toUnreadCount;
    /** lastChatMessageId */
    private Integer lastChatMessageId;
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

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
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

    public Integer getLastChatMessageId() {
        return lastChatMessageId;
    }

    public void setLastChatMessageId(Integer lastChatMessageId) {
        this.lastChatMessageId = lastChatMessageId;
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
        return "Chat{" +
                "id=" + id +
                ", chatType=" + chatType +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", fromUnreadCount=" + fromUnreadCount +
                ", toUnreadCount=" + toUnreadCount +
                ", lastChatMessageId=" + lastChatMessageId +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                ", isDelete=" + isDelete +
                '}';
    }

    /**
     * 转ChatVO
     * @return      ChatVO
     */
    public ChatVO toChatVO() {
        ChatVO chatVO = new ChatVO();
        chatVO.setId(id);
        chatVO.setChatType(chatType);
        chatVO.setFromAccountId(fromAccountId);
        chatVO.setToAccountId(toAccountId);
        chatVO.setFromUnreadCount(fromUnreadCount);
        chatVO.setToUnreadCount(toUnreadCount);
        chatVO.setCreatedTime(createdTime);
        chatVO.setModifiedTime(modifiedTime);
        chatVO.setIsDelete(isDelete);
        return chatVO;
    }
}
