package tel.schich.awss3postsigner;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class S3PostObjectRequest {

    public static final class Builder {

        private final List<Condition> conditions = new ArrayList<>();
        private Instant expiration;
        private String bucket;

        public Builder expiration(Instant timestamp) {
            expiration = timestamp;
            return this;
        }

        public Builder expiration(Duration duration) {
            expiration = Instant.now().plus(duration);
            return this;
        }

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder withCondition(Condition condition) {
            conditions.add(condition);
            return this;
        }

        public S3PostObjectRequest build() {
            Objects.requireNonNull(bucket, "bucket is required!");
            Objects.requireNonNull(expiration, "expiration is required!");
            return new S3PostObjectRequest(conditions, expiration, bucket);
        }
    }

    private final List<Condition> conditions;
    private final Instant expiration;
    private final String bucket;

    private S3PostObjectRequest(List<Condition> conditions, Instant expiration, String bucket) {
        this.conditions = Collections.unmodifiableList(conditions);
        this.expiration = expiration;
        this.bucket = bucket;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public String getBucket() {
        return bucket;
    }
}