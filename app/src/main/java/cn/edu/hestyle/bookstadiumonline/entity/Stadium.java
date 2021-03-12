package cn.edu.hestyle.bookstadiumonline.entity;

import java.io.Serializable;
import java.util.Date;

public class Stadium implements Serializable {
    /**id*/
    private Integer id;
    /**manager id*/
    private Integer stadiumManagerId;
    /**category id*/
    private Integer stadiumCategoryId;
    /**场馆名*/
    private String name;
    /**地址*/
    private String address;
    /**描述*/
    private String description;
    /**照片*/
    private String imagePaths;
    /**是否认证，0未认证，1已认证*/
    private Integer isAuthenticate;
    /**是否删除，0未删除，1已删除，2因违规被拉黑、屏蔽*/
    private Integer isDelete;
    /**创建者*/
    private String createdUser;
    /**创建时间*/
    private Date createdTime;
    /**修改者*/
    private String modifiedUser;
    /**修改时间*/
    private Date modifiedTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStadiumManagerId() {
        return stadiumManagerId;
    }

    public void setStadiumManagerId(Integer stadiumManagerId) {
        this.stadiumManagerId = stadiumManagerId;
    }

    public Integer getStadiumCategoryId() {
        return stadiumCategoryId;
    }

    public void setStadiumCategoryId(Integer stadiumCategoryId) {
        this.stadiumCategoryId = stadiumCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Integer getIsAuthenticate() {
        return isAuthenticate;
    }

    public void setIsAuthenticate(Integer isAuthenticate) {
        this.isAuthenticate = isAuthenticate;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public String toString() {
        return "Stadium{" +
                "id=" + id +
                ", stadiumManagerId=" + stadiumManagerId +
                ", stadiumCategoryId=" + stadiumCategoryId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", imagePaths='" + imagePaths + '\'' +
                ", isAuthenticate=" + isAuthenticate +
                ", isDelete=" + isDelete +
                ", createdUser='" + createdUser + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedUser='" + modifiedUser + '\'' +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}
