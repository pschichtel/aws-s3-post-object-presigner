package tel.schich.awss3postsigner;

import java.util.HashMap;
import java.util.Map;

public class EqualsCondition extends Condition {
    private final String field;
    private final String value;

    EqualsCondition(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    @Override
    Object encode() {
        Map<String, String> data = new HashMap<>(1);
        data.put(field, value);
        return data;
    }
}
