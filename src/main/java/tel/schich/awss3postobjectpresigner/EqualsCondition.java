package tel.schich.awss3postobjectpresigner;

public class EqualsCondition implements Condition {
    private final String field;
    private final String value;

    EqualsCondition(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public String field() {
        return field;
    }

    public String value() {
        return value;
    }
}
