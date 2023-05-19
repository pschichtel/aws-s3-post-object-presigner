package tel.schich.awss3postobjectpresigner;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class S3PresignedPostObjectRequest {
    private final URI uri;
    private final Map<String, String> constantFields;

    public S3PresignedPostObjectRequest(URI uri, Map<String, String> constantFields) {
        this.uri = uri;
        this.constantFields = Collections.unmodifiableMap(constantFields);
    }

    public URI uri() {
        return uri;
    }

    public Map<String, String> constantFields() {
        return constantFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3PresignedPostObjectRequest that = (S3PresignedPostObjectRequest) o;
        return Objects.equals(uri, that.uri) && Objects.equals(constantFields, that.constantFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, constantFields);
    }

    @Override
    public String toString() {
        return "S3PresignedPostObjectRequest{" +
                "uri=" + uri +
                ", fields=" + constantFields +
                '}';
    }
}