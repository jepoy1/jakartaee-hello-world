package org.eclipse.jakarta.purchaseorder.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesInvoice implements Serializable {

    private Long id;
    private String invoiceNumber;
    private Long purchaseOrderId;
    private Long customerId;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
}