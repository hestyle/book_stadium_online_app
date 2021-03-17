package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class UserStadiumBookItem implements Serializable {
    /** userId */
    private Integer userId;
    /** stadiumId */
    private Integer stadiumId;
    /** stadiumName */
    private String stadiumName;
    /** stadiumAddress */
    private String stadiumAddress;
    /** stadiumImagePaths */
    private String stadiumImagePaths;
    /** stadiumBookId */
    private Integer stadiumBookId;
    /** stadiumBook场次起始时间 */
    private Date stadiumBookStartTime;
    /** stadiumBook场次终止时间 */
    private Date stadiumBookEndTime;
    /** stadiumBookItemId */
    private Integer stadiumBookItemId;
    /** 预约时间 */
    private Date stadiumBookedTime;
    /** 场馆评论id，=null表示未评论 */
    private Integer stadiumCommentId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(Integer stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }

    public String getStadiumAddress() {
        return stadiumAddress;
    }

    public void setStadiumAddress(String stadiumAddress) {
        this.stadiumAddress = stadiumAddress;
    }

    public String getStadiumImagePaths() {
        return stadiumImagePaths;
    }

    public void setStadiumImagePaths(String stadiumImagePaths) {
        this.stadiumImagePaths = stadiumImagePaths;
    }

    public Integer getStadiumBookId() {
        return stadiumBookId;
    }

    public void setStadiumBookId(Integer stadiumBookId) {
        this.stadiumBookId = stadiumBookId;
    }

    public Date getStadiumBookStartTime() {
        return stadiumBookStartTime;
    }

    public void setStadiumBookStartTime(Date stadiumBookStartTime) {
        this.stadiumBookStartTime = stadiumBookStartTime;
    }

    public Date getStadiumBookEndTime() {
        return stadiumBookEndTime;
    }

    public void setStadiumBookEndTime(Date stadiumBookEndTime) {
        this.stadiumBookEndTime = stadiumBookEndTime;
    }

    public Integer getStadiumBookItemId() {
        return stadiumBookItemId;
    }

    public void setStadiumBookItemId(Integer stadiumBookItemId) {
        this.stadiumBookItemId = stadiumBookItemId;
    }

    public Date getStadiumBookedTime() {
        return stadiumBookedTime;
    }

    public void setStadiumBookedTime(Date stadiumBookedTime) {
        this.stadiumBookedTime = stadiumBookedTime;
    }

    public Integer getStadiumCommentId() {
        return stadiumCommentId;
    }

    public void setStadiumCommentId(Integer stadiumCommentId) {
        this.stadiumCommentId = stadiumCommentId;
    }

    @Override
    public String toString() {
        return "UserStadiumBookItem{" +
                "userId=" + userId +
                ", stadiumId=" + stadiumId +
                ", stadiumName='" + stadiumName + '\'' +
                ", stadiumAddress='" + stadiumAddress + '\'' +
                ", stadiumImagePaths='" + stadiumImagePaths + '\'' +
                ", stadiumBookId=" + stadiumBookId +
                ", stadiumBookStartTime=" + stadiumBookStartTime +
                ", stadiumBookEndTime=" + stadiumBookEndTime +
                ", stadiumBookItemId=" + stadiumBookItemId +
                ", stadiumBookedTime=" + stadiumBookedTime +
                ", stadiumCommentId=" + stadiumCommentId +
                '}';
    }
}
