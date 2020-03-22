package examples.spring.project.users;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.StringJoiner;

public class UserPojo {

    @Size(min = 4, max = 20, message = "用户名只能是4～20位的字符数字！")
    private String userName;
    @NotBlank(message = "用户昵称不能为空！")
    private String nickName;

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
                .toString();
    }
}
