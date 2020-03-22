package examples.spring.project.users;

import examples.spring.project.Body;
import examples.spring.project.security.JwtUserDetailsService;
import examples.spring.project.security.jwt.JwtTokenProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class LoginController {

    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping(value="/authenticate")
    public ResponseEntity authenticate(@Valid @RequestParam("user_name") @NotBlank(message = "用户名不能为空！") String username,
                                       @RequestParam("password") @NotBlank(message = "密码不能为空！") String password) {
        logger.debug("用户 {} 开始登录。", username);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            String token = jwtTokenProvider.createToken(username, new ArrayList<String>());
            Map<String, Object> model = new HashMap<>();
            model.put("user_name", username);
            model.put("token", token);
            return ok(Body.build().ok("登录成功", model));
        } catch (BadCredentialsException e) {
            return ok(Body.build().fail("账号或密码错误！"));
        }
    }

    @GetMapping("/current_user")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ok(Body.build().ok("成功获取当前用户信息", userDetails));
    }


}
