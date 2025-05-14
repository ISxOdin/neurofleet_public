package ch.zhaw.neurofleet.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {

  public static final String ADMIN = "Bearer admin";
  public static final String OWNER = "Bearer owner";
  public static final String FLEETMANAGER = "Bearer fleetmanager";
  public static final String DRIVER = "Bearer driver";
  public static final String USER = "Bearer user";
  public static final String INVALID = "Bearer invalid";

  @Bean
  public JwtDecoder jwtDecoder() {
    return tokenString -> {
      String bearer = "Bearer " + tokenString;
      if (ADMIN.equals(bearer)) {
        return createJwtWithRoles(List.of("admin"));
      }
      if (OWNER.equals(bearer)) {
        return createJwtWithRoles(List.of("owner"));
      }
      if (FLEETMANAGER.equals(bearer)) {
        return createJwtWithRoles(List.of("fleetmanager"));
      }
      if (DRIVER.equals(bearer)) {
        return createJwtWithRoles(List.of("driver"));
      }
      if (USER.equals(bearer)) {
        return createJwtWithRoles(List.of("user"));
      }
      throw new AuthenticationException("Invalid JWT") {
      };
    };
  }

  private Jwt createJwtWithRoles(List<String> roles) {
    Map<String, Object> headers = Map.of("alg", "none");
    Instant issuedAt = Instant.now();
    Instant expires = issuedAt.plusSeconds(3600);

    Map<String, Object> claims = Map.of(
        "sub", "test-user",
        "user_roles", roles);

    return new Jwt(
        "test-token",
        issuedAt,
        expires,
        headers,
        claims);
  }
}
