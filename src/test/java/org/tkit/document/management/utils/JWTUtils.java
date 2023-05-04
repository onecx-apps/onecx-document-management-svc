package org.tkit.document.management.utils;

import static io.smallrye.jwt.util.KeyUtils.decodePrivateKey;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Map;

import org.eclipse.microprofile.jwt.Claims;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JWTUtils {
    /**
     * Utility method to generate a JWT string from a JSON resource file that is signed by the test_privateKey.pem
     * test resource key, possibly with invalid fields.
     *
     * @param jsonResName - name of test resources file
     * @param timeClaims - used to return the exp, iat, auth_time claims
     * @return the JWT string
     * @throws Exception on parse failure
     */

    public static String generateTokenString(String jsonResName, Map<String, Long> timeClaims)
            throws Exception {
        PrivateKey pk = readPrivateKey("/META-INF/resources/test_privateKey.pem");
        return generateTokenString(pk, "/META-INF/resources/test_privateKey.pem", jsonResName, timeClaims);
    }

    public static String generateTokenString(PrivateKey privateKey, String kid,
            String jsonResName, Map<String, Long> timeClaims) throws Exception {

        JwtClaimsBuilder claims = Jwt.claims(jsonResName);
        long currentTimeInSecs = System.currentTimeMillis() / 1000;
        long exp = timeClaims != null && timeClaims.containsKey(Claims.exp.name())
                ? timeClaims.get(Claims.exp.name())
                : currentTimeInSecs + 300;

        claims.issuedAt(currentTimeInSecs);
        claims.claim(Claims.auth_time.name(), currentTimeInSecs);
        claims.expiresAt(exp);

        return claims.jws().keyId(kid).sign(privateKey);
    }

    /**
     * Read a PEM encoded private key from the classpath
     *
     * @param pemResName - key file resource name
     * @return PrivateKey
     * @throws Exception on decode failure
     */
    public static PrivateKey readPrivateKey(final String pemResName) throws Exception {
        try (InputStream contentIS = JWTUtils.class.getResourceAsStream(pemResName)) {
            byte[] tmp = new byte[4096];
            assert contentIS != null;
            int length = contentIS.read(tmp);
            return decodePrivateKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
