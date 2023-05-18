package tel.schich.awss3postsigner;

import com.google.gson.annotations.Expose;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public final class Policy {

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
            encodedConditions.add(condition.encode());
        }

        return new Policy(expirationString, encodedConditions);
    }
}