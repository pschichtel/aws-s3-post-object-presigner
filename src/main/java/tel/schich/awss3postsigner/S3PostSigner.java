/*
 * MIT License
 *
 * Copyright (c) 2021 Trinopoty Biswas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tel.schich.awss3postsigner;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.Pair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Locale.ENGLISH;

public final class S3PostSigner {

    static final DateTimeFormatter DATESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd", ENGLISH)
            .withZone(UTC);

    /*
     * The hex characters MUST be lower case because AWS only accepts lower case.
     */
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    private static final Gson GSON = new Gson();

    private static final DateTimeFormatter AMZ_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'", ENGLISH)
            .withZone(UTC);

    private static final class Policy {

        @Expose
        public String expiration;

        @Expose
        public List<String[]> conditions;
    }

    private final AwsCredentialsProvider credentialsProvider;

    public S3PostSigner(AwsCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public S3PostSignResponse presignPost(S3PostSignRequest request) {
        final AwsCredentials credentials = credentialsProvider.resolveCredentials();

        final ZonedDateTime date = ZonedDateTime.now(UTC);
        final String credentialsField = buildCredentialField(credentials, request.getRegion());

        final Policy policy = new Policy();
        policy.expiration = (request.getExpiration() != null) ? ISO_INSTANT.format(request.getExpiration()) : null;
        policy.conditions = buildConditions(request, date, credentialsField);
        final String policyJson = GSON.toJson(policy);
        final String policyB64 = Base64.getEncoder().encodeToString(policyJson.getBytes(UTF_8));

        final HashMap<String, String> fields = new HashMap<>();
        fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        fields.put("x-amz-credential", credentialsField);
        fields.put("x-amz-date", AMZ_DATE_FORMATTER.format(date));
        fields.put("x-amz-signature", hexDump(signMac(generateSigningKey(
                        credentials.secretAccessKey(),
                        request.getRegion(),
                        "s3"),
                policyB64.getBytes(UTF_8))));
        fields.put("policy", policyB64);

        if (request.getConditions().containsKey(S3PostSignRequest.ConditionFields.KEY)) {
            fields.put("key", request.getConditions().get(S3PostSignRequest.ConditionFields.KEY).left());
        }

        return new S3PostSignResponse(request.getEndpoint(), fields);
    }

    private static List<String[]> buildConditions(S3PostSignRequest request, ZonedDateTime date, String credentials) {
        final List<String[]> result = new ArrayList<>();
        final Map<S3PostSignRequest.ConditionFields, Pair<String, S3PostSignRequest.ConditionMatch>> conditions = request.getConditions();

        for (Map.Entry<S3PostSignRequest.ConditionFields, Pair<String, S3PostSignRequest.ConditionMatch>> item : conditions.entrySet()) {
            switch (item.getKey()) {
                case BUCKET:
                case CREDENTIAL: {
                    result.add(new String[]{
                            "eq",
                            item.getKey().getName(),
                            item.getValue().left()
                    });
                    break;
                }
                case KEY:
                case CONTENT_TYPE:
                case CONTENT_DISPOSITION:
                case CONTENT_ENCODING:
                case SUCCESS_ACTION_REDIRECT:
                case SUCCESS_ACTION_STATUS: {
                    result.add(new String[]{
                            item.getValue().right().toString(),
                            item.getKey().getName(),
                            item.getValue().left()
                    });
                    break;
                }
                case ACL: {
                    break;
                }
            }
        }

        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.ALGORITHM.getName(), "AWS4-HMAC-SHA256"});
        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.DATE.getName(), AMZ_DATE_FORMATTER.format(date)});
        result.add(new String[]{"eq", S3PostSignRequest.ConditionFields.CREDENTIAL.getName(), credentials});

        return result;
    }

    private static byte[] signMac(byte[] key, byte[] data) {
        try {
            final Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(new SecretKeySpec(key, SIGNATURE_ALGORITHM));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] generateSigningKey(String secretKey, Region region, String service) {
        final byte[] secretKeyBytes = ("AWS4" + secretKey).getBytes(UTF_8);
        final byte[] dateKeyBytes = signMac(secretKeyBytes, DATESTAMP_FORMATTER.format(now()).getBytes(UTF_8));
        final byte[] dateRegionKeyBytes = signMac(dateKeyBytes, region.id().getBytes(UTF_8));
        final byte[] dateRegionServiceKeyBytes = signMac(dateRegionKeyBytes, service.getBytes(UTF_8));
        return signMac(dateRegionServiceKeyBytes, "aws4_request".getBytes(UTF_8));
    }

    private static String buildCredentialField(AwsCredentials credentials, Region region) {
        return credentials.accessKeyId() + "/" +
                DATESTAMP_FORMATTER.format(now()) + "/" +
                region.id() + "/" +
                "s3/aws4_request";
    }

    private static String hexDump(byte[] data) {
        final StringBuilder sb = new StringBuilder();
        for (byte _byte : data) {
            sb.append(HEX_CHARS[(_byte >> 4) & 0xf]);
            sb.append(HEX_CHARS[_byte & 0xf]);
        }
        return sb.toString();
    }
}