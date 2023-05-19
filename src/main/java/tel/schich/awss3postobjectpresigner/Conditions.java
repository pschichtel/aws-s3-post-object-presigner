package tel.schich.awss3postobjectpresigner;

public abstract class Conditions {
    private Conditions() {
    }

    public static EqualsCondition aclEquals(String value) {
        return new EqualsCondition("acl", value);
    }

    public static StartsWithCondition aclStartsWith(String prefix) {
        return new StartsWithCondition("acl", prefix);
    }

    static EqualsCondition bucketEquals(String name) {
        return new EqualsCondition("bucket", name);
    }

    public static ContentLengthRangeCondition contentLengthRange(int minimumBytes, int maximumBytes) {
        return new ContentLengthRangeCondition(minimumBytes, maximumBytes);
    }

    public static EqualsCondition cacheControlHeaderEquals(String value) {
        return new EqualsCondition("Cache-Control", value);
    }

    public static StartsWithCondition cacheControlHeaderStartsWith(String prefix) {
        return new StartsWithCondition("Cache-Control", prefix);
    }

    public static EqualsCondition contentTypeHeaderEquals(String value) {
        return new EqualsCondition("Content-Type", value);
    }

    public static StartsWithCondition contentTypeHeaderStartsWith(String prefix) {
        return new StartsWithCondition("Content-Type", prefix);
    }

    public static EqualsCondition contentDispositionHeaderEquals(String value) {
        return new EqualsCondition("Content-Disposition", value);
    }

    public static StartsWithCondition contentDispositionHeaderStartsWith(String prefix) {
        return new StartsWithCondition("Content-Disposition", prefix);
    }

    public static EqualsCondition contentEncodingHeaderEquals(String value) {
        return new EqualsCondition("Content-Encoding", value);
    }

    public static StartsWithCondition contentEncodingHeaderStartsWith(String prefix) {
        return new StartsWithCondition("Content-Encoding", prefix);
    }

    public static EqualsCondition expiresHeaderEquals(String value) {
        return new EqualsCondition("Expires", value);
    }

    public static StartsWithCondition expiresHeaderStartsWith(String prefix) {
        return new StartsWithCondition("Expires", prefix);
    }

    public static EqualsCondition keyEquals(String value) {
        return new EqualsCondition("key", value);
    }

    public static StartsWithCondition keyStartsWith(String prefix) {
        return new StartsWithCondition("key", prefix);
    }

    public static EqualsCondition successActionRedirectEquals(String value) {
        return new EqualsCondition("success_action_redirect", value);
    }

    public static StartsWithCondition successActionRedirectStartsWith(String prefix) {
        return new StartsWithCondition("success_action_redirect", prefix);
    }

    public static EqualsCondition redirectEquals(String value) {
        return new EqualsCondition("redirect", value);
    }

    public static StartsWithCondition redirectStartsWith(String prefix) {
        return new StartsWithCondition("redirect", prefix);
    }

    public static EqualsCondition successActionStatusEquals(String value) {
        return new EqualsCondition("success_action_status", value);
    }

    static EqualsCondition algorithmEquals(String value) {
        return new EqualsCondition("x-amz-algorithm", value);
    }

    static EqualsCondition credentialEquals(String value) {
        return new EqualsCondition("x-amz-credential", value);
    }

    static EqualsCondition dateEquals(String value) {
        return new EqualsCondition("x-amz-date", value);
    }

    public static EqualsCondition securityTokenEquals(String productToken, String userToken) {
        return new EqualsCondition("x-amz-security-token", productToken + "," + userToken);
    }

    public static EqualsCondition amzMetadataEquals(String name, String value) {
        return new EqualsCondition("x-amz-meta-" + name, value);
    }

    public static StartsWithCondition amzMetadataStartsWith(String name, String prefix) {
        return new StartsWithCondition("x-amz-meta-" + name, prefix);
    }

    public static EqualsCondition amzHeaderEquals(String name, String value) {
        return new EqualsCondition("x-amz-" + name, value);
    }

}
