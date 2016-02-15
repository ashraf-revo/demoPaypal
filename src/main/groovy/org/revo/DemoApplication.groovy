package org.revo

import com.paypal.api.payments.Amount
import com.paypal.api.payments.Details
import com.paypal.api.payments.Item
import com.paypal.api.payments.ItemList
import com.paypal.api.payments.Payer
import com.paypal.api.payments.Payment
import com.paypal.api.payments.PaymentExecution
import com.paypal.api.payments.RedirectUrls
import com.paypal.api.payments.Transaction
import com.paypal.base.rest.APIContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@SpringBootApplication
class DemoApplication {

    static void main(String[] args) {
        SpringApplication.run DemoApplication, args
    }
}

@Controller
class app {
    @Autowired
    APIContext apiContext

    @ResponseBody
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
        RedirectUrls redirectUrls = new RedirectUrls();
        String guid = UUID.randomUUID().toString().replaceAll("-", "");
        String ss = request.scheme + "://" + request.serverName + ":" + request.serverPort + request.contextPath
        redirectUrls.setCancelUrl(ss + "/fail?payerId=" + guid);
        redirectUrls.setReturnUrl(ss + "/success?payerId=" + guid);
        payment.setRedirectUrls(redirectUrls);
        Payment create = payment.create(apiContext);
        print(create)
        create
    }

    @RequestMapping("fail")
    def fail(@RequestParam String guid) {
        guid
    }

    @RequestMapping("success")
    @ResponseBody
    def success(PaypalUser paypalUser, HttpServletRequest request) {
        println(paypalUser)
        Payment payment = new Payment();
        payment.setId(paypalUser.paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(paypalUser.PayerID);
        payment.execute(apiContext.accessToken, paymentExecution);
    }
}
