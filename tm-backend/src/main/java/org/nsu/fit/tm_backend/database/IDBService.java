package org.nsu.fit.tm_backend.database;

import org.nsu.fit.tm_backend.database.data.AccountTokenPojo;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.PlanPojo;
import org.nsu.fit.tm_backend.database.data.SubscriptionPojo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDBService {
    CustomerPojo createCustomer(CustomerPojo customerPojo);

    void editCustomer(CustomerPojo customerPojo);

    void deleteCustomer(UUID id);

    List<CustomerPojo> getCustomers();

    CustomerPojo getCustomer(UUID id);

    CustomerPojo getCustomerByLogin(String customerLogin);

    Optional<CustomerPojo> getOptionalCustomerByLogin(String customerLogin);

    AccountTokenPojo createAccountToken(AccountTokenPojo accountTokenPojo);

    void checkAccountToken(String authenticationToken);

    PlanPojo createPlan(PlanPojo plan);

    void deletePlan(UUID id);

    List<PlanPojo> getPlans();

    SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo);

    void deleteSubscription(UUID id);

    List<SubscriptionPojo> getSubscriptions();

    List<SubscriptionPojo> getSubscriptions(UUID customerId);
}
