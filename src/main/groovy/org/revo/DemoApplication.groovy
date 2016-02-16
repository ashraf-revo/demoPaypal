package org.revo

import com.paypal.api.payments.*
import com.paypal.base.rest.APIContext
import org.revo.autoconfigure.InMemoryPaypalPaymentStorge
import org.revo.autoconfigure.PaypalPaymentStorge
import org.revo.autoconfigure.PaypalUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@SpringBootApplication
class DemoApplication {

    static void main(String[] args) {
        SpringApplication.run DemoApplication, args
    }

    @Bean
    PaypalPaymentStorge storge() {
        new InMemoryPaypalPaymentStorge();
    }

}

@RestController
class app {
    @Autowired
    APIContext apiContext
    @Autowired
    PaypalPaymentStorge storge

    @RequestMapping
    def index(HttpServletRequest request) {
        Details details = new Details();
        details.setShipping("1");
        details.setSubtotal("5");
        details.setTax("1");
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal("7");
        amount.setDetails(details);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");
        Item item = new Item();
        item.setName("Ground Coffee 40 oz").setQuantity("1").setCurrency("USD").setPrice("5");
        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<Item>();
        items.add(item);
        itemList.setItems(items);
        transaction.setItemList(itemList);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        payment.setRedirectUrls(GETRedirectUrls(request, uuid));
        Payment create = payment.create(apiContext);
        storge.save(uuid, create)
        create
    }

    @RequestMapping("fail")
    def fail(PaypalUser paypalUser) {
        paypalUser
    }

    @RequestMapping("success")
    def success(PaypalUser paypalUser, HttpServletRequest request) {
        Payment payment = new Payment();
        payment.setId(paypalUser.paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(paypalUser.payerID);
        payment = Payment.get(apiContext.accessToken, paypalUser.paymentId);
        payment.execute(apiContext.accessToken, paymentExecution);
    }
    @Autowired
    Environment env

    RedirectUrls GETRedirectUrls(HttpServletRequest request, String uuid) {
        RedirectUrls redirectUrls = new RedirectUrls();
        String ss = request.scheme + "://" + request.serverName + ":" + request.serverPort + request.contextPath
        redirectUrls.setCancelUrl(ss + env.getProperty("paypal.failUrl") + "?uuid=" + uuid);
        redirectUrls.setReturnUrl(ss + env.getProperty("paypal.successUrl") + "?uuid=" + uuid);
        redirectUrls
    }
}
