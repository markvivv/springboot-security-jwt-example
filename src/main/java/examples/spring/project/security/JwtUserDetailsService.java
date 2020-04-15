package examples.spring.project.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class JwtUserDetailsService implements UserDetailsService {

    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private SqlSession sqlSession;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // TODO 任意接口每次请求都访问数据库，可以增加jcache
            Map<String, Object> userinfoMap = sqlSession.selectOne("Login.loadUserByUsername", username);
            User user = new User(username, MapUtils.getString(userinfoMap, "nick_name"),
                    MapUtils.getString(userinfoMap, "bcrypt_passwd"),
                    true, true, true, true);
            logger.info("登录用户信息：{}", user);
            return user;
        } catch (Exception e) {
            String msg = "Username: " + username + " not found";
            logger.error(msg, e);
            throw new UsernameNotFoundException(msg);
        }
    }

    public class User implements UserDetails {

        private String username;
        private String nickname;
        @JsonIgnore
        private String password;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;

        public User(String username, String nickname, String password,
                    boolean accountNonExpired, boolean accountNonLocked,
                    boolean credentialsNonExpired, boolean enabled) {
            this.username = username;
            this.nickname = nickname;
            this.password = password;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.enabled = enabled;
        }

        public String getNickname() {
            return nickname;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getPassword() {
            return this.password;
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return this.accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return this.accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return this.credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder("User{");
            sb.append("username='").append(username).append('\'');
            sb.append(", accountNonExpired=").append(accountNonExpired);
            sb.append(", accountNonLocked=").append(accountNonLocked);
            sb.append(", credentialsNonExpired=").append(credentialsNonExpired);
            sb.append(", enabled=").append(enabled);
            sb.append('}');
            return sb.toString();
        }

    }
}
