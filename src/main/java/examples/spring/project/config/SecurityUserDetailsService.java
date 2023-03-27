package examples.spring.project.config;

import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
public class SecurityUserDetailsService implements UserDetailsService {

    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private SqlSession sqlSession;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Map<String, Object> userinfoMap = sqlSession.selectOne("Login.loadUserByUsername", username);
            SecurityUserDetails user = new SecurityUserDetails(username, MapUtils.getString(userinfoMap, "nick_name"),
                    MapUtils.getString(userinfoMap, "bcrypt_passwd"), new ArrayList<>(),
                    true, true, true, true);
            logger.info("登录用户信息：{}", user);
            return user;
        } catch (Exception e) {
            String msg = "Username: " + username + " not found";
            logger.error(msg, e);
            throw new UsernameNotFoundException(msg);
        }
    }

}
