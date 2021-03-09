package cn.edu.hestyle.bookstadiumonline.entity;

import java.util.Date;

public class User {
    public final static String USER_ROLE = "USER_ROLE";

    /**id*/
    private Integer id;
    /**用户名*/
    private String username;
    /**密码*/
    private String password;
    /**盐值*/
    private String saltValue;
    /**头像*/
    private String avatarPath;
    /**性别*/
    private String gender;
    /**地址*/
    private String address;
    /**电话号码*/
    private String phoneNumber;
    /**信用积分*/
    private Integer creditScore;
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
    /**token*/
    private String token;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSaltValue() {
        return saltValue;
    }

    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", saltValue='" + saltValue + '\'' +
                ", avatarPath='" + avatarPath + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", creditScore=" + creditScore +
                ", isAuthenticate=" + isAuthenticate +
                ", isDelete=" + isDelete +
                ", createdUser='" + createdUser + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedUser='" + modifiedUser + '\'' +
                ", modifiedTime=" + modifiedTime +
                ", token='" + token + '\'' +
                '}';
    }
}
