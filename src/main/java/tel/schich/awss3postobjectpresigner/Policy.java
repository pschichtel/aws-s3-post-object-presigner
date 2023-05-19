package tel.schich.awss3postobjectpresigner;

import com.google.gson.annotations.Expose;

import java.time.Instant;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.singletonMap;

final class Policy {

    @Expose
    public final String expiration;

    @Expose
    public final List<Object> conditions;

    Policy(String expiration, List<Object> conditions) {
        this.expiration = expiration;
        this.conditions = conditions;
    }

    public static Policy create(Instant expiration, List<Condition> conditions) {
        String expirationString = (expiration != null) ? ISO_INSTANT.format(expiration) : null;
        ArrayList<Object> encodedConditions = new ArrayList<>();

        for (Condition condition : conditions) {
            if (condition instanceof EqualsCondition) {
                EqualsCondition cond = (EqualsCondition) condition;
                encodedConditions.add(singletonMap(cond.field(), cond.value()));
            } else if (condition instanceof StartsWithCondition) {
                StartsWithCondition cond = (StartsWithCondition) condition;
                encodedConditions.add(new Object[] { "starts-with", "$" + cond.field(), cond.prefix()});
            } else if (condition instanceof ContentLengthRangeCondition) {
                ContentLengthRangeCondition cond = (ContentLengthRangeCondition) condition;
                encodedConditions.add(new Object[] { "content-length-range", cond.minimumBytes(), cond.maximumBytes() });
            } else {
                throw new IllegalArgumentException("Unknown Condition type: " + condition.getClass().getName());
            }
        }

        return new Policy(expirationString, encodedConditions);
    }
}