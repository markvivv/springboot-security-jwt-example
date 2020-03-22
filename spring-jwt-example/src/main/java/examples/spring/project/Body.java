package examples.spring.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pagehelper.PageInfo;

import java.util.Map;
import java.util.StringJoiner;

public class Body {

    private int status = 200;
    private String message = "";

    @JsonProperty("page_num")
    private Long pageNum;
    private Long total;
    @JsonProperty("page_size")
    private Long pageSize;

    private Object data;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public Long getTotal() {
        return total;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public Object getData() {
        return data;
    }

    public static Body build() {
        return new Body();
    }

    public Body ok(String message) {
        this.status = 200;
        this.message = message;
        return this;
    }

    public Body ok(String message, Object data) {
        this.status = 200;
        this.message = message;
        this.data = data;
        return this;
    }

    public Body ok(String msg, Map<String, Object> data) {
        this.status = 200;
        this.message = msg;
        this.data = data;
        return this;
    }

    public Body ok(String msg, PageInfo pageInfo) {
        this.status = 200;
        this.message = msg;
        this.data = pageInfo.getList();
        this.pageNum = Long.valueOf(pageInfo.getPageNum());
        this.total = Long.valueOf(pageInfo.getTotal());
        this.pageSize = Long.valueOf(pageInfo.getPageSize());
        return this;
    }

    public Body fail(String msg) {
        this.status = 400;
        this.message = msg;
        return this;
    }

    public Body fail(String msg, Map<String, Object> data) {
        this.status = 400;
        this.message = msg;
        this.data = data;
        return this;
    }

    public Body internalServerError(String msg) {
        this.status = 500;
        this.message = msg;
        return this;
    }

    public Body internalServerError(String msg, Map<String, Object> data) {
        this.status = 500;
        this.message = msg;
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Body.class.getSimpleName() + "[", "]")
                .add("status=" + status)
                .add("message='" + message + "'")
                .add("data=" + data)
                .toString();
    }
}
