package org.nsu.fit.tm_backend.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.nsu.fit.tm_backend.database.IDBService;
import org.nsu.fit.tm_backend.database.data.ContactPojo;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.TopUpBalancePojo;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Globals;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerManager extends ParentManager {

    private static final int CONDITION_NAME_LENGTH_MINIMAL_INCLUDE = 2;
    private static final int CONDITION_NAME_LENGTH_MAXIMUM_INCLUDE = 12;
    private static final int CONDITION_PASS_LENGTH_MINIMAL_INCLUDE = 6;
    private static final int CONDITION_PASS_LENGTH_MAXIMUM_INCLUDE = 12;
    private static final int CONDITION_PASSWORD_PART_LENGTH_EXCLUDE = 3;

    private static final Set<String> SIMPLE_PASSWORD_SET = Stream.of("123qwe", "1q2w3e")
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    public CustomerManager(IDBService dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает новый объект класса Customer. Ограничения:
     * Аргумент 'customer' - не null;
     * firstName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * lastName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * login - указывается в виде email, проверить email на корректность, проверить что нет customer с таким же email;
     * pass - длина от 6 до 12 символов включительно, не должен быть простым (123qwe или 1q2w3e), не должен содержать части login, firstName, lastName
     * balance - должно быть равно 0 перед отправкой базу данных.
     */
    public CustomerPojo createCustomer(CustomerPojo customer) {
        requireNonNull(customer, "Argument 'customer' is null.");

        checkName(requireNonNull(customer.firstName, "Field 'customer.firstName' is null."), "customer.firstName");
        checkName(requireNonNull(customer.lastName, "Field 'customer.lastName' is null."), "customer.lastName");
        checkEmail(requireNonNull(customer.login, "Field 'customer.login' is null."));
        checkPass(customer);

        // Лабораторная 2: добавить код который бы проверял, что нет customer'а c таким же login (email'ом).
        // Попробовать добавить другие ограничения, посмотреть как быстро растет кодовая база тестов.
        dbService.getOptionalCustomerByLogin(customer.getLogin())
                .ifPresent(customerFromDb -> {
                    throw new IllegalArgumentException(String.format("Customer with login[%s] already exist", customer.getLogin()));
                });

        checkBalance(customer.balance);
        return dbService.createCustomer(customer);
    }

    private static void checkBalance(int balance) {
        if (balance != 0) {
            throw new IllegalArgumentException("Balance must be equals 0");
        }
    }

    private static void checkPass(@NotNull CustomerPojo customer) {
        final String pass = requireNonNull(customer.pass, "Field 'customer.pass' is null.");

        if (pass.length() < CONDITION_PASS_LENGTH_MINIMAL_INCLUDE || pass.length() > CONDITION_PASS_LENGTH_MAXIMUM_INCLUDE) {
            throw new IllegalArgumentException(
                    String.format(
                            "Password's length should be more or equal %d symbols and less or equal %d symbols.",
                            CONDITION_PASS_LENGTH_MINIMAL_INCLUDE,
                            CONDITION_PASS_LENGTH_MAXIMUM_INCLUDE));
        }

        if (SIMPLE_PASSWORD_SET.contains(pass.toLowerCase())) {
            throw new IllegalArgumentException("Password is very easy.");
        }

        final String lowerCasePass = pass.toLowerCase();
        final Set<String> invalidPartsFromPass = Stream.of(List.of(customer.firstName, customer.lastName),
                        Arrays.asList(customer.login.split("[^A-Za-z0-9]")))
                .flatMap(Collection::stream)
                .map(String::toLowerCase)
                .filter(str -> str.length() > CONDITION_PASSWORD_PART_LENGTH_EXCLUDE)
                .filter(lowerCasePass::contains)
                .collect(Collectors.toSet());
        if (!invalidPartsFromPass.isEmpty()) {
            throw new IllegalArgumentException(String.format("Password contains not secure items: %s", invalidPartsFromPass));
        }
    }

    @NotNull
    private static <T> T requireNonNull(@Nullable T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    private static void checkName(@NotNull String name, String fieldName) {
        int length = name.length();
        if (length < CONDITION_NAME_LENGTH_MINIMAL_INCLUDE || length > CONDITION_NAME_LENGTH_MAXIMUM_INCLUDE) {
            throw new IllegalArgumentException(
                    String.format("%s's length should be more or equal %d symbols and less or equal %d symbols.",
                            fieldName, CONDITION_NAME_LENGTH_MINIMAL_INCLUDE, CONDITION_NAME_LENGTH_MAXIMUM_INCLUDE));
        }
        if (Character.isLowerCase(name.charAt(0))) {
            throw new IllegalArgumentException(String.format("%s contain first symbol is lower case, but must be upper case", fieldName));
        }
        if (!StringUtils.isAllLowerCase(name.substring(1))) {
            throw new IllegalArgumentException(String.format("%s contain not first symbol is upper case, but must be lower case", fieldName));

        }
        final Set<String> invalidChars = name.chars()
                .filter(codePoint -> !Character.isAlphabetic(codePoint))
                .mapToObj(Character::toString)
                .collect(Collectors.toSet());

        if (!invalidChars.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s contains symbol %s, but must only alphabet", fieldName, invalidChars));

        }
    }

    private static void checkEmail(@Nullable String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new IllegalArgumentException(String.format("login %s is invalid, must be correctly email", email));
        }
    }

    /**
     * Метод возвращает список customer'ов.
     */
    public List<CustomerPojo> getCustomers() {
        return dbService.getCustomers();
    }

    public CustomerPojo getCustomer(UUID customerId) {
        return dbService.getCustomer(customerId);
    }

    public CustomerPojo lookupCustomer(String login) {
        return dbService.getCustomers().stream()
                .filter(x -> x.login.equals(login))
                .findFirst()
                .orElse(null);
    }

    public ContactPojo me(AuthenticatedUserDetails authenticatedUserDetails) {
        ContactPojo contactPojo = new ContactPojo();

        if (authenticatedUserDetails.isAdmin()) {
            contactPojo.login = Globals.ADMIN_LOGIN;

            return contactPojo;
        }

        // Лабораторная 2: обратите внимание что вернули данных больше чем надо...
        // т.е. getCustomerByLogin честно возвратит все что есть в базе данных по этому customer'у.
        // необходимо написать такой unit тест, который бы отлавливал данное поведение.
        return dbService.getCustomerByLogin(authenticatedUserDetails.getName());
    }

    public void deleteCustomer(UUID id) {
        dbService.deleteCustomer(id);
    }

    /**
     * Метод добавляет к текущему баласу переданное значение, которое должно быть строго больше нуля.
     */
    public CustomerPojo topUpBalance(TopUpBalancePojo topUpBalancePojo) {
        CustomerPojo customerPojo = dbService.getCustomer(topUpBalancePojo.customerId);

        customerPojo.balance += topUpBalancePojo.money;

        dbService.editCustomer(customerPojo);

        return customerPojo;
    }
}
