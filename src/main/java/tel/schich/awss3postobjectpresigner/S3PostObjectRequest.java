package tel.schich.awss3postobjectpresigner;

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
            return new S3PostObjectRequest(this);
        }
    }

    private final List<Condition> conditions;
    private final Instant expiration;
    private final String bucket;

    private S3PostObjectRequest(Builder builder) {
        Objects.requireNonNull(builder.bucket, "bucket is required!");
        Objects.requireNonNull(builder.expiration, "expiration is required!");
        this.conditions = Collections.unmodifiableList(builder.conditions);
        this.expiration = builder.expiration;
        this.bucket = builder.bucket;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Condition> conditions() {
        return conditions;
    }

    public Instant expiration() {
        return expiration;
    }

    public String bucket() {
        return bucket;
    }
}