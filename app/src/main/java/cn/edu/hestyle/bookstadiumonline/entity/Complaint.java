package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class Complaint implements Serializable {
    /** 账号类型 */
    public static final Integer COMPLAIN_ACCOUNT_TYPE_USER = 1;
    public static final Integer COMPLAIN_ACCOUNT_TYPE_STADIUM_MANAGER = 2;

    /** id */
    private Integer id;
    /** 投诉人账号类型 */
    private Integer complainantAccountType;
    /** 投诉人账号id，可能是用户id，也可能是动态、评论id */
    private Integer complainantAccountId;
    /** 被投诉人账号类型User、StadiumManager等 */
    private Integer respondentAccountType;
    /** 被投诉人的账号id */
    private Integer respondentAccountId;
    /** title */
    private String title;
    /** description */
    private String description;
    /** imagePaths */
    private String imagePaths;
    /** 投诉时间 */
    private Date complainedTime;
    /** 是否已处理 */
    private Integer hasHandled;
    /** 投诉处理时间 */
    private Date handledTime;
    /** complainant处理分数 */
    private Integer complainantHandleCreditScore;
    /** complainant处理描述 */
    private String complainantHandleDescription;
    /** respondent处理分数 */
    private Integer respondentHandleCreditScore;
    /** respondent处理描述 */
    private String respondentHandleDescription;
    /** 是否删除，0未删除，1已删除 */
    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getComplainantAccountType() {
        return complainantAccountType;
    }

    public void setComplainantAccountType(Integer complainantAccountType) {
        this.complainantAccountType = complainantAccountType;
    }

    public Integer getComplainantAccountId() {
        return complainantAccountId;
    }

    public void setComplainantAccountId(Integer complainantAccountId) {
        this.complainantAccountId = complainantAccountId;
    }

    public Integer getRespondentAccountType() {
        return respondentAccountType;
    }

    public void setRespondentAccountType(Integer respondentAccountType) {
        this.respondentAccountType = respondentAccountType;
    }

    public Integer getRespondentAccountId() {
        return respondentAccountId;
    }

    public void setRespondentAccountId(Integer respondentAccountId) {
        this.respondentAccountId = respondentAccountId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public Date getComplainedTime() {
        return complainedTime;
    }

    public void setComplainedTime(Date complainedTime) {
        this.complainedTime = complainedTime;
    }

    public Integer getHasHandled() {
        return hasHandled;
    }

    public void setHasHandled(Integer hasHandled) {
        this.hasHandled = hasHandled;
    }

    public Date getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(Date handledTime) {
        this.handledTime = handledTime;
    }

    public Integer getComplainantHandleCreditScore() {
        return complainantHandleCreditScore;
    }

    public void setComplainantHandleCreditScore(Integer complainantHandleCreditScore) {
        this.complainantHandleCreditScore = complainantHandleCreditScore;
    }

    public String getComplainantHandleDescription() {
        return complainantHandleDescription;
    }

    public void setComplainantHandleDescription(String complainantHandleDescription) {
        this.complainantHandleDescription = complainantHandleDescription;
    }

    public Integer getRespondentHandleCreditScore() {
        return respondentHandleCreditScore;
    }

    public void setRespondentHandleCreditScore(Integer respondentHandleCreditScore) {
        this.respondentHandleCreditScore = respondentHandleCreditScore;
    }

    public String getRespondentHandleDescription() {
        return respondentHandleDescription;
    }

    public void setRespondentHandleDescription(String respondentHandleDescription) {
        this.respondentHandleDescription = respondentHandleDescription;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", complainantAccountType=" + complainantAccountType +
                ", complainantAccountId=" + complainantAccountId +
                ", respondentAccountType=" + respondentAccountType +
                ", respondentAccountId=" + respondentAccountId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imagePaths='" + imagePaths + '\'' +
                ", complainedTime=" + complainedTime +
                ", hasHandled=" + hasHandled +
                ", handledTime=" + handledTime +
                ", complainantHandleCreditScore=" + complainantHandleCreditScore +
                ", complainantHandleDescription='" + complainantHandleDescription + '\'' +
                ", respondentHandleCreditScore=" + respondentHandleCreditScore +
                ", respondentHandleDescription='" + respondentHandleDescription + '\'' +
                ", isDelete=" + isDelete +
                '}';
    }
}
