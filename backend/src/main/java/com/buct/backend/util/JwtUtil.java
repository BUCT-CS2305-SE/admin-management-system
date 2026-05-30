package com.buct.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.buct.backend.common.AuthUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String ISSUER = "admin-management-system";
    private static final String SECRET = "admin-management-system-hjj-secret";
    private static final long EXPIRE_HOURS = 8L;

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private final JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();

    public String createToken(AuthUser user) {
        Date expireAt = Date.from(getExpireAt().atZone(ZoneId.systemDefault()).toInstant());
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(String.valueOf(user.getUserId()))
                .withClaim("userId", user.getUserId())
                .withClaim("username", user.getUsername())
                .withClaim("userType", user.getUserType())
                .withClaim("roleId", user.getRoleId())
                .withExpiresAt(expireAt)
                .sign(algorithm);
    }

    public AuthUser verify(String token) {
        DecodedJWT jwt = verifier.verify(token);
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();
        String userType = jwt.getClaim("userType").asString();
        Long roleId = jwt.getClaim("roleId").asLong();
        return new AuthUser(userId, username, userType, roleId);
    }

    public LocalDateTime getExpireAt() {
        return LocalDateTime.now().plusHours(EXPIRE_HOURS);
    }
}
