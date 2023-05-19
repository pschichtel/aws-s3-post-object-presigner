package tel.schich.awss3postobjectpresigner;

public class StartsWithCondition implements Condition {
    private final String field;
    private final String prefix;

    StartsWithCondition(String field, String prefix) {
        this.field = field;
        this.prefix = prefix;
    }

    public String field() {
        return field;
    }

    public String prefix() {
        return prefix;
    }
}
