package net.chrisrichardson.eventstore.javaexamples.banking.backend.queryside.customers;

import net.chrisrichardson.eventstore.EntityWithIdAndVersion;
import net.chrisrichardson.eventstore.EventStore;
import net.chrisrichardson.eventstore.javaexamples.banking.backend.commandside.customers.Customer;
import net.chrisrichardson.eventstore.javaexamples.banking.backend.commandside.customers.CustomerService;
import net.chrisrichardson.eventstore.javaexamples.banking.backend.queryside.accounts.AccountInfo;
import net.chrisrichardson.eventstore.javaexamples.banking.common.customers.Address;
import net.chrisrichardson.eventstore.javaexamples.banking.common.customers.CustomerInfo;
import net.chrisrichardson.eventstore.javaexamples.banking.common.customers.Name;
import net.chrisrichardson.eventstorestore.javaexamples.testutil.Producer;
import net.chrisrichardson.eventstorestore.javaexamples.testutil.Verifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;

import static net.chrisrichardson.eventstorestore.javaexamples.testutil.TestUtil.await;
import static net.chrisrichardson.eventstorestore.javaexamples.testutil.TestUtil.eventually;

/**
 * Created by Main on 10.02.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CustomerQuerySideTestConfiguration.class)
@IntegrationTest
public class CustomerQuerySideIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerQueryService customerQueryService;

    @Autowired
    private EventStore eventStore;

    @Test
    public void shouldCreateCustomer() throws Exception {
        CustomerInfo customerInfo = generateCustomerInfo();
        EntityWithIdAndVersion<Customer> customer = await(customerService.createCustomer(customerInfo));

        Thread.sleep(10000);
        eventually(
                new Producer<QuerySideCustomer>() {
                    @Override
                    public Observable<QuerySideCustomer> produce() {
                        return customerQueryService.findByCustomerId(customer.getEntityIdentifier());
                    }
                },
                new Verifier<QuerySideCustomer>() {
                    @Override
                    public void verify(QuerySideCustomer querySideCustomer) {
                        Assert.assertEquals(customerInfo.getName(), querySideCustomer.getName());
                        Assert.assertEquals(customerInfo.getSsn(), querySideCustomer.getSsn());
                        Assert.assertEquals(customerInfo.getEmail(), querySideCustomer.getEmail());
                        Assert.assertEquals(customerInfo.getPhoneNumber(), querySideCustomer.getPhoneNumber());
                        Assert.assertEquals(customerInfo.getAddress(), querySideCustomer.getAddress());
                    }
                });
    }

    private CustomerInfo generateCustomerInfo() {
        return new CustomerInfo(
                new Name("John", "Doe"),
                "current@email.com",
                "000-00-0000",
                "1-111-111-1111",
                new Address("street 1",
                        "street 2",
                        "City",
                        "State",
                        "1111111")
        );
    }
}
