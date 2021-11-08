package org.nsu.fit.tm_backend.database.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerPojo extends ContactPojo {
    @JsonProperty("id")
    public UUID id;

    @Override
    public String toString() {
        return new StringJoiner(", ", CustomerPojo.class.getSimpleName() + "[", "]")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("login='" + login + "'")
                .add("pass='" + pass + "'")
                .add("balance=" + balance)
                .add("id=" + id)
                .toString();
    }

    public UUID getId() {
        return id;
    }
}
