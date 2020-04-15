package examples.spring.project.security.jwt;

import io.jsonwebtoken.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private Logger logger = LogManager.getLogger();

    @Resource
    private UserDetailsService userDetailsService;

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
     * @param username
     * @param roles
     * @return
     */
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
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
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
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
