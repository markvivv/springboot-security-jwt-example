package examples.spring.project.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.StringJoiner;

public class UserPojo {

    @Size(min = 4, max = 20, message = "用户名只能是4～20位的字符数字！")
    private String userName;
    @NotBlank(message = "用户昵称不能为空！")
    private String nickName;
    private String password;
    @JsonIgnore
    private String bcryptPasswd;
    private String status;
    private Date createDate;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getBcryptPasswd() {
        return bcryptPasswd;
    }

    public void setBcryptPasswd(String bcryptPasswd) {
        this.bcryptPasswd = bcryptPasswd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserPojo.class.getSimpleName() + "[", "]")
                .add("userName='" + userName + "'")
                .add("nickName='" + nickName + "'")
                .add("password=******('" + password.length() + ")'")
                .add("status='" + status + "'")
                .add("createDate=" + createDate)
                .toString();
    }
}
