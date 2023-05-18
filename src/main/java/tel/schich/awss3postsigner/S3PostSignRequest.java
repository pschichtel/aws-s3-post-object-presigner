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

import software.amazon.awssdk.awscore.endpoint.DefaultServiceEndpointBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.Pair;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class S3PostSignRequest {

    public enum ConditionMatch {
        EXACT,
        STARTS_WITH;


        @Override
        public String toString() {
            switch (this) {
                case EXACT:
                    return "eq";
                case STARTS_WITH:
                    return "starts-with";
            }
            return null;
        }
    }

    public enum ConditionFields {
        BUCKET("$bucket"),
        KEY("$key"),
        ACL("$acl"),
        CONTENT_LENGTH_RANGE("$content-length-range"),
        CONTENT_TYPE("$Content-Type"),
        CONTENT_DISPOSITION("$Content-Disposition"),
        CONTENT_ENCODING("$Content-Encoding"),
        SUCCESS_ACTION_REDIRECT("$success_action_redirect"),
        SUCCESS_ACTION_STATUS("success_action_status"),
        ALGORITHM("$x-amz-algorithm"),
        CREDENTIAL("$x-amz-credential"),
        DATE("$x-amz-date");

        private final String name;

        ConditionFields(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class Builder {

        private final EnumMap<ConditionFields, Pair<String, ConditionMatch>> conditions =
                new EnumMap<>(ConditionFields.class);

        private Region region = Region.US_EAST_1;
        private URI endpoint;
        private ZonedDateTime expiration;

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder withEndpoint(String endpoint) {
            this.endpoint = URI.create(endpoint);
            return this;
        }

        public Builder withExpiration(ZonedDateTime timestamp) {
            expiration = timestamp.withZoneSameInstant(ZoneOffset.UTC);
            return this;
        }

        public Builder withExpiration(Duration duration) {
            expiration = ZonedDateTime.now(ZoneOffset.UTC).plus(duration);
            return this;
        }

        public Builder withBucket(String bucket) {
            conditions.put(ConditionFields.BUCKET, Pair.of(bucket, ConditionMatch.EXACT));
            return this;
        }

        public Builder withKey(String key) {
            return withKey(key, ConditionMatch.EXACT);
        }

        public Builder withKey(String key, ConditionMatch match) {
            conditions.put(ConditionFields.KEY, Pair.of(key, match));
            return this;
        }

        public S3PostSignRequest build() {
            URI endpoint = this.endpoint;
            if (endpoint == null) {
                endpoint = (new DefaultServiceEndpointBuilder("s3", "https"))
                        .withRegion(region)
                        .getServiceEndpoint();
            }

            return new S3PostSignRequest(conditions, region, endpoint, expiration);
        }
    }

    private final Map<ConditionFields, Pair<String, ConditionMatch>> conditions;
    private final Region region;
    private final URI endpoint;
    private final ZonedDateTime expiration;

    private S3PostSignRequest(EnumMap<ConditionFields, Pair<String, ConditionMatch>> conditions,
                              Region region,
                              URI endpoint,
                              ZonedDateTime expiration) {
        this.conditions = Collections.unmodifiableMap(conditions);
        this.region = region;
        this.endpoint = endpoint;
        this.expiration = expiration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<ConditionFields, Pair<String, ConditionMatch>> getConditions() {
        return conditions;
    }

    public Region getRegion() {
        return region;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public ZonedDateTime getExpiration() {
        return expiration;
    }
}