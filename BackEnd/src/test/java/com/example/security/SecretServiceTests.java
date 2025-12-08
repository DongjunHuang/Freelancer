package com.example.security;


import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class SecretServiceTests {
    private static final String TEST_KEY = "0123456789_0123456789_0123456789_0123";

    private SecretService newService() {
        return new SecretService(TEST_KEY);
    }

    @Test
    void generateAccessToken_shouldBeValid_andParseCorrectClaims() {
        // given
        SecretService service = newService();
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
        SecretService service = newService();
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
    void isValid_shouldReturnFalse_whenTokenIsRandomString() {
        // given
        SecretService service = newService();

        // when / assert
        assertThat(service.isValid("abc.def.ghi")).isFalse();
        assertThat(service.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenSignedWithDifferentKey() {
        // given
        SecretService serviceA = new SecretService(TEST_KEY);
        SecretService serviceB = new SecretService("another_secure_key_1234567890_abcdef");

        String tokenFromA = serviceA.generateAccessToken("user1", "u1@example.com");

        // when / assert
        assertThat(serviceA.isValid(tokenFromA)).isTrue();
        assertThat(serviceB.isValid(tokenFromA)).isFalse(); 
    }

    @Test
    void generateSignedDeviceId_shouldReturnValueWithSignature_andValidateTrue() {
        // given
        SecretService service = newService();

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
    void validateSignedDeviceId_shouldReturnFalse_whenDeviceIdTampered() {
        // given
        SecretService service = newService();
        String signed = service.generateSignedDeviceId();
        String[] parts = signed.split("\\.");

        String tampered = "xxx-" + parts[0] + "." + parts[1];

        // when / then
        assertThat(service.validateSignedDeviceId(tampered)).isFalse();
    }

    @Test
    void validateSignedDeviceId_shouldReturnFalse_whenSignatureTampered() {
        // given
        SecretService service = newService();
        String signed = service.generateSignedDeviceId();
        String[] parts = signed.split("\\.");

        String tamperedSig = parts[1] + "abc";
        String tampered = parts[0] + "." + tamperedSig;

        // when / assert
        assertThat(service.validateSignedDeviceId(tampered)).isFalse();
    }
}