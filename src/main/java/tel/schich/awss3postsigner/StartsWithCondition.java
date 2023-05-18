package tel.schich.awss3postsigner;

public class StartsWithCondition extends Condition {
    private final String field;
    private final String prefix;

    StartsWithCondition(String field, String prefix) {
        this.field = field;
        this.prefix = prefix;
    }

    @Override
    Object[] encode() {
        return new Object[] { "starts-with", "$" + field, prefix};
    }
}
