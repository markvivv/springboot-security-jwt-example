package examples.spring.project.users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.StringJoiner;

public class UserPojo {

    private String userName;
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
