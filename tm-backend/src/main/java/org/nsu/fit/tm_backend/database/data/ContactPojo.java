package org.nsu.fit.tm_backend.database.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactPojo {
    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("login")
    public String login;

    /**
     * Лабораторная *: здесь следует обратить внимание на хранение и передачу пароля
     * в открытом виде, почему это плохо, как можно исправить.
     */
    @JsonProperty("password")
    public String pass;

    @JsonProperty("balance")
    public int balance;

    @Override
    public String toString() {
        return new StringJoiner(", ", ContactPojo.class.getSimpleName() + "[", "]")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("login='" + login + "'")
                .add("pass='" + pass + "'")
                .add("balance=" + balance)
                .toString();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public int getBalance() {
        return balance;
    }
}
