package tech.ydb.apps.model;

/**
 *
 * @author Aleksandr Gorshenin
 */
public class PaymentResponse {
    private final String status;

    public PaymentResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
