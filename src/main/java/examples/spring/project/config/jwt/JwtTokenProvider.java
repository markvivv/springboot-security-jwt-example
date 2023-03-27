package examples.spring.project.config.jwt;

import examples.spring.project.config.InvalidJwtAuthenticationException;
import examples.spring.project.config.SecurityUserDetails;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private Logger logger = LogManager.getLogger();

    //@Autowired
    //private UserDetailsService userDetailsService;

    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${jwt.validtiy}")
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration validityInMs;

    @PostConstruct
    protected void initial() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * 根据账号、角色信息创建token
     *
     * @param securityUserDetails
     * @return
     */
    public String createToken(SecurityUserDetails securityUserDetails) {
        Claims claims = Jwts.claims().setSubject(securityUserDetails.getUsername());
        // 存储业务用各种数据，保存非涉密信息，比如用户名、昵称、所属企业等
        claims.put("roles", securityUserDetails.getAuthorities());
        claims.put("nickname", securityUserDetails.getNickname());
        return generateToken(claims);
    }

    /**
     * 刷新token信息
     *
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return generateToken(claims);
    }

    private String generateToken(Claims claims) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMs.toMillis());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                // 指定签名用 SecretKey，或使用非对称加密，指定 PrivateKey 进行签名，并使用公钥加密内容.
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        // 方法一：每次接口请求时，在JwtTokenOncePerRequestFilter会调用本方法，每次读取数据库。也可以考虑增加缓存。
        //UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        // 方法二：token有效期内，根据token解码的信息，重新生成SecurityUserDetails信息
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

        SecurityUserDetails userDetails = new SecurityUserDetails(
                claims.getSubject(),
                claims.get("nickname", String.class),
                "", claims.get("roles", List.class),
                true, true, true, true);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public Date getTokenExpiration(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

            if (claims.getExpiration().before(new Date())) {
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token. {}", token);
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Invalid JWT token");
        }
    }

}
