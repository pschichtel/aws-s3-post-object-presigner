package tel.schich.awss3postobjectpresigner;

public class ContentLengthRangeCondition implements Condition {
    private final int minimumBytes;
    private final int maximumBytes;

    ContentLengthRangeCondition(int minimumBytes, int maximumBytes) {
        this.minimumBytes = minimumBytes;
        this.maximumBytes = maximumBytes;
    }

    public int minimumBytes() {
        return minimumBytes;
    }

    public int maximumBytes() {
        return maximumBytes;
    }
}
