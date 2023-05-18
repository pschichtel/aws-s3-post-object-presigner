package tel.schich.awss3postsigner;

import com.google.gson.Gson;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.endpoint.DefaultServiceEndpointBuilder;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.profiles.ProfileFileSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.util.Locale.ENGLISH;

public final class S3PostObjectPresigner {

    public static final class Builder {
        private S3Configuration serviceConfiguration = S3Configuration.builder().build();
        private AwsCredentialsProvider credentialsProvider;
        private URI endpointOverride = null;
        private Region region = null;

        public Builder serviceConfiguration(S3Configuration serviceConfiguration) {
            Objects.requireNonNull(serviceConfiguration);
            this.serviceConfiguration = serviceConfiguration;
            return this;
        }

        public Builder credentialsProvider(AwsCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        public Builder endpointOverride(URI endpointOverride) {
            this.endpointOverride = endpointOverride;
            return this;
        }

        public Builder region(Region region) {
            this.region = region;
            return this;
        }

        public S3PostObjectPresigner build() {
            return new S3PostObjectPresigner(this);
        }
    }

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


    private final Supplier<ProfileFile> profileFile;
    private final String profileName;
    private final AwsCredentialsProvider credentialsProvider;
    private final S3Configuration serviceConfiguration;
    private final URI endpoint;

    private final Region region;

    S3PostObjectPresigner(Builder builder) {
        this.profileFile = ProfileFile::defaultProfileFile;
        this.profileName = ProfileFileSystemSetting.AWS_PROFILE.getStringValueOrThrow();
        this.serviceConfiguration = builder.serviceConfiguration;
        this.region = builder.region != null ? builder.region : DefaultAwsRegionProviderChain.builder()
                .profileFile(profileFile)
                .profileName(profileName)
                .build()
                .getRegion();
        this.credentialsProvider = builder.credentialsProvider != null ? builder.credentialsProvider : DefaultCredentialsProvider.builder()
                .profileFile(profileFile)
                .profileName(profileName)
                .build();
        this.endpoint = builder.endpointOverride != null ? builder.endpointOverride : new DefaultServiceEndpointBuilder("s3", "https")
                .withRegion(region)
                .withProfileFile(profileFile)
                .withProfileName(profileName)
                .getServiceEndpoint();
    }

    public S3PresignedPostObjectRequest presignPost(S3PostObjectRequest request) {
        final AwsCredentials credentials = credentialsProvider.resolveCredentials();

        final Instant timestamp = Instant.now();
        final String credentialsField = buildCredentialField(credentials, region);

        final List<Condition> augmentedConditions = new ArrayList<>(request.getConditions());
        augmentedConditions.add(Conditions.algorithmEquals("AWS4-HMAC-SHA256"));
        augmentedConditions.add(Conditions.credentialEquals(credentialsField));
        augmentedConditions.add(Conditions.dateEquals(AMZ_DATE_FORMATTER.format(timestamp)));
        augmentedConditions.add(Conditions.bucketEquals(request.getBucket()));
        final Policy policy = Policy.create(request.getExpiration(), augmentedConditions);

        final HashMap<String, String> fields = new HashMap<>();

        for (Condition condition : augmentedConditions) {
            if (condition instanceof EqualsCondition) {
                final EqualsCondition equalsCondition = (EqualsCondition) condition;
                fields.put(equalsCondition.getField(), equalsCondition.getValue());
            }
        }

        final String encodedPolicy = Base64.getEncoder().encodeToString(GSON.toJson(policy).getBytes(UTF_8));
        fields.put("x-amz-signature", hexDump(signMac(generateSigningKey(
                        credentials.secretAccessKey(),
                        timestamp,
                        region,
                        "s3"),
                encodedPolicy.getBytes(UTF_8))));
        fields.put("Policy", encodedPolicy);

        URI endpoint;
        if (serviceConfiguration.pathStyleAccessEnabled()) {
            endpoint = this.endpoint.resolve(URLEncoder.encode(request.getBucket(), UTF_8));
        } else {
            try {
                String hostWithBucket = request.getBucket() + "." + this.endpoint.getHost();
                endpoint = new URI(this.endpoint.getScheme(),
                        this.endpoint.getUserInfo(),
                        hostWithBucket,
                        this.endpoint.getPort(),
                        this.endpoint.getPath(),
                        this.endpoint.getQuery(),
                        this.endpoint.getFragment());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return new S3PresignedPostObjectRequest(endpoint, fields);
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

    private static byte[] generateSigningKey(String secretKey, Instant timestamp, Region region, String service) {
        final byte[] secretKeyBytes = ("AWS4" + secretKey).getBytes(UTF_8);
        final byte[] dateKeyBytes = signMac(secretKeyBytes, DATESTAMP_FORMATTER.format(timestamp).getBytes(UTF_8));
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

    public static Builder builder() {
        return new Builder();
    }
}