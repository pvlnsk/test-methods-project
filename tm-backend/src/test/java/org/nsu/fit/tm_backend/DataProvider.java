package org.nsu.fit.tm_backend;

import org.nsu.fit.tm_backend.database.data.ContactPojo;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;

import java.util.Set;
import java.util.UUID;

public final class DataProvider {

    public static AuthenticatedUserDetails createUserDetailsForRoles(String id, String name, String... roles) {
        return new AuthenticatedUserDetails(
                id,
                name,
                Set.of(roles));
    }

    public static ContactPojo createContact() {
        ContactPojo contactPojo = new ContactPojo();
        contactPojo.login = "test_login";
        contactPojo.firstName = "John";
        contactPojo.lastName = "Wick";
        contactPojo.pass = "strong_pass_Z5x4c";
        contactPojo.balance = 100;
        return contactPojo;
    }

    public static CustomerPojo createCustomer() {
        CustomerPojo customerPojo = new CustomerPojo();
        customerPojo.id = UUID.randomUUID();
        customerPojo.firstName = "John";
        customerPojo.lastName = "Wick";
        customerPojo.login = "john_wick@example.com";
        customerPojo.pass = "strongZ5x4c";
        customerPojo.balance = 0;
        return customerPojo;
    }

    public static SubscriptionPojo creatSubscription(UUID customerId, int planFee) {
        SubscriptionPojo subscriptionPojo = new SubscriptionPojo();
        subscriptionPojo.customerId = customerId;
        subscriptionPojo.id = UUID.randomUUID();
        subscriptionPojo.planFee = planFee;
        subscriptionPojo.planDetails = "description";
        subscriptionPojo.planId = UUID.randomUUID();
        subscriptionPojo.planName = "test_plan_name";
        return subscriptionPojo;
    }

    private DataProvider() {
    }
}
