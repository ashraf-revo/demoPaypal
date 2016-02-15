package org.revo

import com.paypal.api.payments.Payment
import groovy.transform.Canonical

/**
 * Created by ashraf on 2/15/2016.
 */
@Canonical
class PaypalUser {
    String paymentId
    String token
    String PayerID
}
