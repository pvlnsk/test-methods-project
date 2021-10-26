package org.nsu.fit.tm_backend.operations;

import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.manager.CustomerManager;
import org.nsu.fit.tm_backend.manager.SubscriptionManager;

import java.util.List;
import java.util.UUID;

public class StatisticOperation implements Operation<StatisticOperation.StatisticOperationResult> {
    private final CustomerManager customerManager;
    private final SubscriptionManager subscriptionManager;
    private final List<UUID> customerIds;

    public StatisticOperation(
            CustomerManager customerManager,
            SubscriptionManager subscriptionManager,
            List<UUID> customerIds) {
        if (customerManager == null) {
            throw new IllegalArgumentException("customerManager");
        }

        if (subscriptionManager == null) {
            throw new IllegalArgumentException("subscriptionManager");
        }

        if (customerIds == null) {
            throw new IllegalArgumentException("customerIds");
        }

        this.customerManager = customerManager;
        this.subscriptionManager = subscriptionManager;
        this.customerIds = customerIds;
    }

    @Override
    public StatisticOperationResult Execute() {
        StatisticOperationResult result = new StatisticOperationResult();

        result.customerIds = customerIds;
        for (UUID customerId : customerIds) {
            CustomerPojo customer = customerManager.getCustomer(customerId);
            result.overallBalance += customer.balance;

            List<SubscriptionPojo> subscriptions = subscriptionManager.getSubscriptions(customerId);
            for (SubscriptionPojo subscription : subscriptions) {
                result.overallFee += subscription.planFee;
            }
        }

        return result;
    }

    public static class StatisticOperationResult {
        // Список идентификаторов customer'ов.
        private List<UUID> customerIds;

        // Их общий остаточный баланс.
        public int overallBalance;

        // Их общая сумма денег потраченных покупку различных планов.
        public int overallFee;

        public List<UUID> getCustomerIds() {
            return customerIds;
        }

        public int getOverallBalance() {
            return overallBalance;
        }

        public int getOverallFee() {
            return overallFee;
        }
    }
}
