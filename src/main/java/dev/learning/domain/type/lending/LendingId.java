package dev.learning.domain.type.lending;

public record LendingId(String id)
{
    static LendingId from(String id)
    {
        //generate randomID
        var randomId = java.util.UUID.randomUUID().toString();
        return new LendingId("LND-" + randomId);
    }
    public LendingId {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Invalid lending ID: " + id);
    }
}
