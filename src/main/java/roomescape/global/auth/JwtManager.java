package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.LoginMember;
import roomescape.domain.member.Member;

@Component
public class JwtManager {

    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public String createToken(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setSubject(member.getId().toString())
            .claim(CLAIM_NAME, member.getName())
            .claim(CLAIM_EMAIL, member.getEmail())
            .claim(CLAIM_ROLE, member.getRole())
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public LoginMember findMember(HttpServletRequest request) {
        LoginMember defaultMember = new LoginMember("-1", "", "", "NOT_REGISTERED");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return defaultMember;
        }

        String token = extractTokenFromCookies(cookies);
        if (token == null) {
            return defaultMember;
        }

        return parse(token);
    }

    private String extractTokenFromCookies(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private LoginMember parse(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();

        String id = claims.getSubject();
        String name = claims.get(CLAIM_NAME, String.class);
        String email = claims.get(CLAIM_EMAIL, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);

        return new LoginMember(id, email, name, role);
    }
}

