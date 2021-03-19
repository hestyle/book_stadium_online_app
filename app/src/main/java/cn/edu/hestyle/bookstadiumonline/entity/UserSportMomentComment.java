package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class UserSportMomentComment implements Serializable {
    /** id */
    private Integer id;
    /** userId */
    private Integer userId;
    /** commentUsername */
    private String commentUsername;
    /** commentUserAvatarPath */
    private String commentUserAvatarPath;
    /** sportMomentId */
    private Integer sportMomentId;
    /** parentId == null表示是sportMoment的评论，否则表示是评论的回复 */
    private Integer parentId;
    /** 回复的评论（原评论）的content */
    private String parentContent;
    /** 评论内容 */
    private String content;
    /** 评论时间 */
    private Date commentedTime;
    /** 点赞数量 */
    private Integer likeCount;
    /** 是否删除，0未删除，1已删除，2因违规被拉黑、屏蔽 */
    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCommentUsername() {
        return commentUsername;
    }

    public void setCommentUsername(String commentUsername) {
        this.commentUsername = commentUsername;
    }

    public String getCommentUserAvatarPath() {
        return commentUserAvatarPath;
    }

    public void setCommentUserAvatarPath(String commentUserAvatarPath) {
        this.commentUserAvatarPath = commentUserAvatarPath;
    }

    public Integer getSportMomentId() {
        return sportMomentId;
    }

    public void setSportMomentId(Integer sportMomentId) {
        this.sportMomentId = sportMomentId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getParentContent() {
        return parentContent;
    }

    public void setParentContent(String parentContent) {
        this.parentContent = parentContent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCommentedTime() {
        return commentedTime;
    }

    public void setCommentedTime(Date commentedTime) {
        this.commentedTime = commentedTime;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "UserSportMomentComment{" +
                "id=" + id +
                ", userId=" + userId +
                ", commentUsername='" + commentUsername + '\'' +
                ", commentUserAvatarPath='" + commentUserAvatarPath + '\'' +
                ", sportMomentId=" + sportMomentId +
                ", parentId=" + parentId +
                ", parentContent='" + parentContent + '\'' +
                ", content='" + content + '\'' +
                ", commentedTime=" + commentedTime +
                ", likeCount=" + likeCount +
                ", isDelete=" + isDelete +
                '}';
    }
}
