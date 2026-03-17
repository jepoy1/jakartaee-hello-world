package org.eclipse.jakarta.purchaseorder.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesInvoiceItem implements Serializable {

    private Long id;
    private String invoiceNumber;
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;
}