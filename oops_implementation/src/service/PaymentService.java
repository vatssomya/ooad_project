// service/PaymentService.java
package service;
import model.Payment;
import java.util.UUID;
public class PaymentService {
    public Payment processPayment(double amount, String method) {
        // mock success
        String txn = method + "-" + UUID.randomUUID().toString().substring(0,8);
        return new Payment(UUID.randomUUID().toString(), amount, method, "SUCCESS", txn);
    }
}
