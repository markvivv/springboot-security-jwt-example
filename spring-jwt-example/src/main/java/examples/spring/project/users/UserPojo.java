package examples.spring.project.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.StringJoiner;

public class UserPojo {

    @Size(min = 4, max = 20, message = "用户名只能是4～20位的字符数字！")
    private String username;
    @NotBlank(message = "用户昵称不能为空！")
    private String nickname;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserPojo.class.getSimpleName() + "[", "]")
                .add("userName='" + username + "'")
                .add("nickName='" + nickname + "'")
                .add("password=******('" + password.length() + ")'")
                .add("status='" + status + "'")
                .add("createDate=" + createDate)
                .toString();
    }
}
