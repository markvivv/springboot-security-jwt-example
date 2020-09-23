package examples.spring.project.users;

import examples.spring.project.Body;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private SqlSession sqlSession;

    @PostMapping(value="/add_user")
    public ResponseEntity addUser(@Valid @RequestBody UserPojo userPojo) {
        logger.debug("添加用户信息: {}", userPojo);
        userPojo.setBcryptPasswd(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(userPojo.getPassword()));
        int result = sqlSession.insert("User.addUser", userPojo);
        if (result == 1) {
            return ok(Body.build().ok("添加用户成功。", userPojo));
        } else {
            return ok(Body.build().fail("添加用户失败。"));
        }
    }

    @PostMapping(value="/edit_user")
    public ResponseEntity editUser(@Valid @RequestBody UserPojo userPojo) {
        return ok(Body.build().ok("编辑用户信息成功！", userPojo));
    }

    @PostMapping(value="/delete_user")
    public ResponseEntity deleteUser(@Valid @RequestBody UserPojo userPojo) {
        return ok(Body.build().ok("成功删除用户信息！", userPojo));
    }

}
