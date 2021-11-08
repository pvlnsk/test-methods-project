package org.nsu.fit.tm_backend.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.DataProvider;
import org.nsu.fit.tm_backend.database.IDBService;
import org.nsu.fit.tm_backend.database.data.ContactPojo;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.TopUpBalancePojo;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Лабораторная 2: покрыть юнит тестами класс CustomerManager на 100%.
@ExtendWith(MockitoExtension.class)
class CustomerManagerTest {

    @Mock
    private Logger logger;
    @Mock
    private IDBService dbService;
    @InjectMocks
    private CustomerManager customerManager;
    @Captor
    private ArgumentCaptor<CustomerPojo> customerPojoCaptor;

    @Test
    void testCreateCustomer() {
        CustomerPojo customerPojo = DataProvider.createCustomer();
        CustomerPojo expectedCustomer = DataProvider.createCustomer();
        //because refEq() checks the "==" objects reference first
        expectedCustomer.id = customerPojo.id;

        customerManager.createCustomer(customerPojo);

        verify(dbService).createCustomer(refEq(expectedCustomer));
    }

    @Test
    void testCreateCustomerWithSameLogin() {
        CustomerPojo customerPojo = DataProvider.createCustomer();
        when(dbService.getOptionalCustomerByLogin(customerPojo.login)).thenReturn(Optional.of(customerPojo));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customerManager.createCustomer(customerPojo));
        assertEquals("Customer with login[john_wick@example.com] already exist", exception.getMessage());
    }

    @ParameterizedTest()
    @MethodSource("provideInvalidCustomers")
    void testCreateCustomerWithInvalidInputArguments(Supplier<CustomerPojo> supplier, String errorMessage) {
        final CustomerPojo customer = supplier.get();
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> customerManager.createCustomer(customer));

        assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> provideInvalidCustomers() {
        return Stream.of(
                Arguments.of((Supplier<CustomerPojo>) () -> null,
                        "Argument 'customer' is null."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = null;
                            return customerPojo;
                        },
                        "Field 'customer.pass' is null."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = "a".repeat(5);
                            return customerPojo;
                        },
                        "Password's length should be more or equal 6 symbols and less or equal 12 symbols."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = "a".repeat(13);
                            return customerPojo;
                        },
                        "Password's length should be more or equal 6 symbols and less or equal 12 symbols."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = "123qwe";
                            return customerPojo;
                        },
                        "Password is very easy."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = "1q2w3e";
                            return customerPojo;
                        },
                        "Password is very easy."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = customerPojo.getFirstName() + customerPojo.getLastName();
                            return customerPojo;
                        },
                        "Password contains not secure items: [john, wick]"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.pass = customerPojo.getLastName() + "123";
                            return customerPojo;
                        },
                        "Password contains not secure items: [wick]"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.firstName = "a";
                            return customerPojo;
                        },
                        "customer.firstName's length should be more or equal 2 symbols and less or equal 12 symbols."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.firstName = "a".repeat(13);
                            return customerPojo;
                        },
                        "customer.firstName's length should be more or equal 2 symbols and less or equal 12 symbols."),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.firstName = "1marat";
                            return customerPojo;
                        },
                        "customer.firstName contains symbol [1], but must only alphabet"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.firstName = customerPojo.firstName.toLowerCase();
                            return customerPojo;
                        },
                        "customer.firstName contain first symbol is lower case, but must be upper case"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.firstName = "AA" + customerPojo.firstName;
                            return customerPojo;
                        },
                        "customer.firstName contain not first symbol is upper case, but must be lower case"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.login = customerPojo.login.replace("@", "#");
                            return customerPojo;
                        },
                        "login john_wick#example.com is invalid, must be correctly email"),
                Arguments.of((Supplier<CustomerPojo>) () -> {
                            CustomerPojo customerPojo = DataProvider.createCustomer();
                            customerPojo.balance = 1;
                            return customerPojo;
                        },
                        "Balance must be equals 0")
        );
    }

    @Test
    void testGetCustomers() {
        List<CustomerPojo> expectedList = List.of(DataProvider.createCustomer());
        when(dbService.getCustomers()).thenReturn(expectedList);

        List<CustomerPojo> actualList = customerManager.getCustomers();

        assertEquals(expectedList, actualList,
                () -> String.format("Expected: %s, actual: %s", expectedList, actualList));
    }

    @Test
    void testGetCustomerForId() {
        CustomerPojo expected = DataProvider.createCustomer();
        UUID expectedUUID = UUID.randomUUID();
        expected.id = expectedUUID;
        when(dbService.getCustomer(expectedUUID)).thenReturn(expected);

        CustomerPojo actualCustomer = customerManager.getCustomer(expectedUUID);

        assertEquals(expected, actualCustomer,
                () -> String.format("Expected: %s, actual: %s", expected, actualCustomer));
        verify(dbService).getCustomer(expectedUUID);
    }

    @Test
    void testLookupCustomer() {
        CustomerPojo expectedCustomer = DataProvider.createCustomer();
        List<CustomerPojo> expectedList = List.of(expectedCustomer);
        when(dbService.getCustomers()).thenReturn(expectedList);

        assertNull(customerManager.lookupCustomer("something"));
        CustomerPojo actualCustomer = customerManager.lookupCustomer(expectedCustomer.login);

        assertEquals(expectedCustomer, actualCustomer,
                () -> String.format("Expected: %s, actual: %s", expectedCustomer, actualCustomer));
        verify(dbService, times(2)).getCustomers();
    }

    @Test
    void testMeForCustomer() {
        CustomerPojo expectedCustomerPojo = DataProvider.createCustomer();
        AuthenticatedUserDetails userDetails = DataProvider.createUserDetailsForRoles(
                UUID.randomUUID().toString(),
                expectedCustomerPojo.login,
                Authority.CUSTOMER_ROLE);
        when(dbService.getCustomerByLogin(userDetails.getName())).thenReturn(expectedCustomerPojo);

        ContactPojo actualContactPojo = customerManager.me(userDetails);

//        assertNull(actualContactPojo.pass);
        assertTrue(new ReflectionEquals(expectedCustomerPojo, "pass").matches(actualContactPojo),
                () -> String.format("Expected: %s, actual: %s", expectedCustomerPojo, actualContactPojo));
        verify(dbService).getCustomerByLogin(userDetails.getName());
    }

    @Test
    void testMeForAdmin() {
        ContactPojo expectedContactPojo = new ContactPojo();
        expectedContactPojo.login = Globals.ADMIN_LOGIN;
        AuthenticatedUserDetails userDetails = DataProvider.createUserDetailsForRoles(
                UUID.randomUUID().toString(),
                "something",
                Authority.ADMIN_ROLE);
        ContactPojo actualContactPojo = customerManager.me(userDetails);

        assertTrue(new ReflectionEquals(expectedContactPojo).matches(actualContactPojo),
                () -> String.format("Expected: %s, actual: %s", expectedContactPojo, actualContactPojo));
        verify(dbService, times(0)).getCustomerByLogin(anyString());
    }

    @Test
    void testDeleteCustomer() {
        UUID uuid = UUID.randomUUID();
        customerManager.deleteCustomer(uuid);
        verify(dbService).deleteCustomer(uuid);
    }

    @Test
    void testDeleteCustomerWithNullObject() {
        UUID uuid = null;
        assertDoesNotThrow(() -> customerManager.deleteCustomer(uuid));
        verify(dbService).deleteCustomer(uuid);
    }

    @Test
    void testTopUpBalance() {
        CustomerPojo expectedCustomer = DataProvider.createCustomer();
        UUID uuid = expectedCustomer.id;
        when(dbService.getCustomer(uuid)).thenReturn(expectedCustomer);
        TopUpBalancePojo upBalancePojo = new TopUpBalancePojo();
        upBalancePojo.customerId = uuid;
        int increasedAmount = 100;
        upBalancePojo.money = increasedAmount;
        int expectedAmount = expectedCustomer.balance + increasedAmount;

        customerManager.topUpBalance(upBalancePojo);

        verify(dbService).editCustomer(customerPojoCaptor.capture());
        CustomerPojo actualCustomer = customerPojoCaptor.getValue();
        assertEquals(expectedCustomer.id, actualCustomer.id,
                () -> String.format("Expected: %s, actual: %s", expectedCustomer, actualCustomer));
        assertEquals(expectedAmount, actualCustomer.balance,
                () -> String.format("Expected: %s, actual: %s", expectedCustomer, actualCustomer));
    }

}
