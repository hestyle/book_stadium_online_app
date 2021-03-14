package cn.edu.hestyle.bookstadiumonline.util;

/**
 * 服务器端向客户端响应结果的类型
 * @param <E> 服务器端向客户端响应数据的类型
 */
public class ResponseResult<E> {
    /** DATE字段转json格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 操作成功的code */
    public static final Integer SUCCESS = 200;

    /**
     * 状态码
     */
    private Integer code;
    /**
     * 异常信息
     */
    private String message;
    /**
     * 数据
     */
    private E data;
    /**
     * 数量
     */
    private Integer count;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", count=" + count +
                '}';
    }
}