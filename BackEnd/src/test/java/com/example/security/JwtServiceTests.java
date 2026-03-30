package com.example.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class JwtServiceTests {
    private static final String PRIVATE_KEY = "0123456789_0123456789_0123456789_0123";
    private static final long ACCESS_EXPIRATION = 300000;
    private static final long REFRESH_EXPIRATION = 300000;
    private static final long ADMIN_ACCESS_EXPIRATION = 300000;
    JwtService service;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setPrivateKey(PRIVATE_KEY);
        properties.setAccessExpiration(ACCESS_EXPIRATION);
        properties.setRefreshExpiration(REFRESH_EXPIRATION);
        properties.setAdminAccessExpiration(ADMIN_ACCESS_EXPIRATION);
        service = new JwtService(properties);
    }

    @Test
    void generateAccessToken_shouldBeValid_andParseCorrectClaims() {
        // given
        String username = "alice";
        String email = "alice@example.com";

        // when
        String token = service.generateAccessToken(username, email);

        // assert
        assertThat(token).isNotBlank();
        assertThat(service.isValid(token)).isTrue();

        TokenInfo info = service.parse(token);
        assertThat(info.getUsername()).isEqualTo(username);
        assertThat(info.getEmail()).isEqualTo(email);
    }

    @Test
    void generateRefreshToken_shouldBeValid_andParseCorrectClaims() {
        // given
        String username = "bob";
        String email = "bob@example.com";

        // when
        String token = service.generateRefreshToken(username, email);

        // assert
        assertThat(token).isNotBlank();
        assertThat(service.isValid(token)).isTrue();

        TokenInfo info = service.parse(token);
        assertThat(info.getUsername()).isEqualTo(username);
        assertThat(info.getEmail()).isEqualTo(email);
    }

    @Test
    void isValidShouldReturnFalseWhenTokenIsRandomString() {
        // when / assert
        assertThat(service.isValid("abc.def.ghi")).isFalse();
        assertThat(service.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void isValidShouldReturnFalseWhenSignedWithDifferentKey() {
        // given
        JwtProperties secondProperties = new JwtProperties();
        secondProperties.setPrivateKey("123dasdasrtgregre==----qeweqwe12321312312fsdfdsfds");
        secondProperties.setAccessExpiration(ACCESS_EXPIRATION);
        secondProperties.setRefreshExpiration(REFRESH_EXPIRATION);
        secondProperties.setAdminAccessExpiration(ADMIN_ACCESS_EXPIRATION);
        JwtService secondService = new JwtService(secondProperties);

        String tokenFromA = service.generateAccessToken("user1", "u1@example.com");

        // when / assert
        assertThat(service.isValid(tokenFromA)).isTrue();
        assertThat(secondService.isValid(tokenFromA)).isFalse();
    }

    @Test
    void generateSignedDeviceIdShouldReturnValueWithSignatureAndValidateTrue() {
        // when
        String signed = service.generateSignedDeviceId();

        // assert
        assertThat(signed).contains(".");
        String[] parts = signed.split("\\.");
        assertThat(parts).hasSize(2);
        assertThat(parts[0]).isNotBlank();
        assertThat(parts[1]).isNotBlank();

        assertThat(service.validateSignedDeviceId(signed)).isTrue();
    }

    @Test
    void validateSignedDeviceIdShouldReturnFalseWhenDeviceIdTampered() {
        // given
        String signed = service.generateSignedDeviceId();
        String[] parts = signed.split("\\.");

        String tampered = "xxx-" + parts[0] + "." + parts[1];

        // when / then
        assertThat(service.validateSignedDeviceId(tampered)).isFalse();
    }

    @Test
    void validateSignedDeviceIdShouldReturnFalseWhenSignatureTampered() {
        // given
        String signed = service.generateSignedDeviceId();
        String[] parts = signed.split("\\.");

        String tamperedSig = parts[1] + "abc";
        String tampered = parts[0] + "." + tamperedSig;

        // when / assert
        assertThat(service.validateSignedDeviceId(tampered)).isFalse();
    }
}