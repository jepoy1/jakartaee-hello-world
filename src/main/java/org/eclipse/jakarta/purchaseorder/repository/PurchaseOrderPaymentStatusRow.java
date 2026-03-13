package org.eclipse.jakarta.purchaseorder.repository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderPaymentStatusRow {
    private Long purchaseOrderId;
    private String paymentStatus;
}
