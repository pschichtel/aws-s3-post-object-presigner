package tel.schich.awss3postobjectpresigner;

public class ContentLengthRangeCondition extends Condition {
    private final int minimumBytes;
    private final int maximumBytes;

    ContentLengthRangeCondition(int minimumBytes, int maximumBytes) {
        this.minimumBytes = minimumBytes;
        this.maximumBytes = maximumBytes;
    }

    @Override
    Object[] encode() {
        return new Object[] { "content-length-range", minimumBytes, maximumBytes };
    }
}
