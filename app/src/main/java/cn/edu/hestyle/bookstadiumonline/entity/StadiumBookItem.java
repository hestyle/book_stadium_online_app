package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class StadiumBookItem implements Serializable {
    /** id */
    private Integer id;
    /** stadiumBook id */
    private Integer stadiumBookId;
    /** user id */
    private Integer userId;
    /** username */
    private String username;
    /** userAvatarPath */
    private String userAvatarPath;
    /** 预约时间 */
    private Date bookedTime;
    /** stadiumCommentId 未评论时=null */
    private Integer stadiumCommentId;
    /**是否删除，0未删除，1已删除，2因违规被拉黑、屏蔽*/
    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStadiumBookId() {
        return stadiumBookId;
    }

    public void setStadiumBookId(Integer stadiumBookId) {
        this.stadiumBookId = stadiumBookId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatarPath() {
        return userAvatarPath;
    }

    public void setUserAvatarPath(String userAvatarPath) {
        this.userAvatarPath = userAvatarPath;
    }

    public Date getBookedTime() {
        return bookedTime;
    }

    public void setBookedTime(Date bookedTime) {
        this.bookedTime = bookedTime;
    }

    public Integer getStadiumCommentId() {
        return stadiumCommentId;
    }

    public void setStadiumCommentId(Integer stadiumCommentId) {
        this.stadiumCommentId = stadiumCommentId;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "StadiumBookItem{" +
                "id=" + id +
                ", stadiumBookId=" + stadiumBookId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", userAvatarPath='" + userAvatarPath + '\'' +
                ", bookedTime=" + bookedTime +
                ", stadiumCommentId=" + stadiumCommentId +
                ", isDelete=" + isDelete +
                '}';
    }
}
