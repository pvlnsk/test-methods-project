package org.nsu.fit.tm_backend.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.DataProvider;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.manager.CustomerManager;
import org.nsu.fit.tm_backend.manager.SubscriptionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticOperationTest {

    private static final CustomerPojo FIRST_CUSTOMER = DataProvider.createCustomer();
    private static final CustomerPojo SECOND_CUSTOMER = DataProvider.createCustomer();
    private static final List<CustomerPojo> CUSTOMERS_LIST = List.of(FIRST_CUSTOMER, SECOND_CUSTOMER);


    private static final int INIT_BALANCE = 100;
    private static final int PLAN_FEE = 50;

    static {
        CUSTOMERS_LIST.forEach(customer -> customer.balance += INIT_BALANCE);
    }

    @Mock
    private CustomerManager customerManager;
    @Mock
    private SubscriptionManager subscriptionManager;
    @Spy
    private List<UUID> customerIds = new ArrayList<>(CUSTOMERS_LIST
            .stream()
            .map(CustomerPojo::getId)
            .collect(Collectors.toList()));
    @InjectMocks
    private StatisticOperation statisticOperation;

    @Test
    void constructorTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticOperation(null, subscriptionManager, customerIds));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticOperation(customerManager, null, customerIds));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticOperation(customerManager, subscriptionManager, null));
        assertDoesNotThrow(() -> new StatisticOperation(customerManager, subscriptionManager, customerIds));
    }

    @Test
    void execute() {
        when(customerManager.getCustomer(FIRST_CUSTOMER.id)).thenReturn(FIRST_CUSTOMER);
        when(customerManager.getCustomer(SECOND_CUSTOMER.id)).thenReturn(SECOND_CUSTOMER);
        when(subscriptionManager.getSubscriptions(FIRST_CUSTOMER.id)).thenReturn(List.of(DataProvider.creatSubscription(FIRST_CUSTOMER.id, PLAN_FEE)));
        when(subscriptionManager.getSubscriptions(SECOND_CUSTOMER.id)).thenReturn(List.of(DataProvider.creatSubscription(SECOND_CUSTOMER.id, PLAN_FEE)));

        StatisticOperation.StatisticOperationResult actualResult = statisticOperation.Execute();

        assertNotNull(actualResult);
        assertEquals(customerIds, actualResult.getCustomerIds());
        assertEquals(CUSTOMERS_LIST.stream().mapToInt(CustomerPojo::getBalance).sum(), actualResult.getOverallBalance());
        assertEquals(PLAN_FEE * CUSTOMERS_LIST.size(), actualResult.getOverallFee());
    }
}
