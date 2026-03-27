package org.eclipse.jakarta.purchaseorder.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.model.Product;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrderItem;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoice;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoiceItem;

public interface PurchaseOrderQueryMapper {

    @Select("SELECT po.id FROM purchase_order po ORDER BY po.id")
    List<Long> findAllPurchaseOrderIds();

    @Select({
        "<script>",
        "SELECT po.id, po.order_number, po.customer_id, po.order_date",
        "FROM purchase_order po",
        "WHERE po.id IN",
        "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "ORDER BY po.id",
        "</script>"
    })
    @Results(id = "purchaseOrderResultMap", value = {
        @Result(column = "id", property = "id", id = true),
        @Result(column = "order_number", property = "orderNumber"),
        @Result(column = "order_date", property = "orderDate"),
        @Result(column = "customer_id", property = "customer", one = @One(select = "findCustomerById")),
        @Result(column = "id", property = "items", many = @Many(select = "findPurchaseOrderItemsByPurchaseOrderId"))
    })
    List<PurchaseOrder> findPurchaseOrdersByIds(@Param("ids") List<Long> ids);

    @Select("SELECT po.id, po.order_number, po.customer_id, po.order_date FROM purchase_order po WHERE po.id = #{id}")
    @ResultMap("purchaseOrderResultMap")
    PurchaseOrder findPurchaseOrderById(@Param("id") Long id);

    @Select({
        "SELECT po.id, po.order_number, po.customer_id, po.order_date",
        "FROM purchase_order po",
        "JOIN customer c ON c.id = po.customer_id",
        "WHERE po.order_number = #{orderNumber}",
        "AND c.name = #{customerName}",
        "AND po.order_date = #{orderDate}",
        "LIMIT 1"
    })
    @ResultMap("purchaseOrderResultMap")
    PurchaseOrder findPurchaseOrderByOrderNumberCustomerNameAndOrderDate(
        @Param("orderNumber") String orderNumber,
        @Param("customerName") String customerName,
        @Param("orderDate") java.time.LocalDate orderDate
    );

    @Select("SELECT po.id, po.order_number, po.customer_id, po.order_date FROM purchase_order po WHERE po.id = #{id}")
    @Results(id = "purchaseOrderDetailResultMap", value = {
        @Result(column = "id", property = "id", id = true),
        @Result(column = "order_number", property = "orderNumber"),
        @Result(column = "order_date", property = "orderDate"),
        @Result(column = "customer_id", property = "customer", one = @One(select = "findCustomerById")),
        @Result(column = "id", property = "items", many = @Many(select = "findPurchaseOrderItemsByPurchaseOrderId")),
        @Result(column = "id", property = "salesInvoiceItems", many = @Many(select = "findSalesInvoiceItemsByPurchaseOrderId"))
    })
    PurchaseOrder findDetailedPurchaseOrderById(@Param("id") Long id);

    @Insert("INSERT INTO purchase_order (order_number, customer_id, order_date) VALUES (#{orderNumber}, #{customer.id}, #{orderDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPurchaseOrder(PurchaseOrder purchaseOrder);

    @Insert("INSERT INTO customer (name, email) VALUES (#{name}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCustomer(Customer customer);

        @Insert({
            "INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_price)",
            "VALUES (#{purchaseOrderId}, #{productId}, #{quantity}, #{unitPrice})"
        })
        int insertPurchaseOrderItem(
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity,
            @Param("unitPrice") java.math.BigDecimal unitPrice
        );

    @Update("UPDATE purchase_order SET order_number = #{orderNumber}, customer_id = #{customer.id}, order_date = #{orderDate} WHERE id = #{id}")
    int updatePurchaseOrder(PurchaseOrder purchaseOrder);

    @Delete("DELETE FROM purchase_order WHERE id = #{id}")
    int deletePurchaseOrderById(@Param("id") Long id);

    @Select("SELECT c.id, c.name, c.email FROM customer c WHERE c.id = #{id}")
    Customer findCustomerById(@Param("id") Long id);

    @Select("SELECT c.id, c.name, c.email FROM customer c WHERE c.name = #{name}")
    Customer findCustomerByName(@Param("name") String name);

    @Select("SELECT c.name FROM customer c ORDER BY c.name")
    List<String> findAllCustomerNames();

    @Select("SELECT p.product_name FROM products p ORDER BY p.product_name")
    List<String> findAllProductNames();

    @Insert("INSERT INTO products (product_name, description) VALUES (#{productName}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProduct(Product product);

    @Select({
        "SELECT poi.id, poi.purchase_order_id, poi.product_id, poi.quantity, poi.unit_price",
        "FROM purchase_order_items poi",
        "WHERE poi.purchase_order_id = #{purchaseOrderId}",
        "ORDER BY poi.id"
    })
    @Results(id = "purchaseOrderItemResultMap", value = {
        @Result(column = "id", property = "id", id = true),
        @Result(column = "quantity", property = "quantity"),
        @Result(column = "unit_price", property = "unitPrice"),
        @Result(column = "product_id", property = "product", one = @One(select = "findProductById"))
    })
    List<PurchaseOrderItem> findPurchaseOrderItemsByPurchaseOrderId(@Param("purchaseOrderId") Long purchaseOrderId);

    @Select("SELECT p.id, p.product_name, p.description FROM products p WHERE p.id = #{id}")
    Product findProductById(@Param("id") Long id);

    @Select("SELECT p.id, p.product_name, p.description FROM products p WHERE p.product_name = #{name}")
    Product findProductByName(@Param("name") String name);

    @Insert({
        "INSERT INTO sales_invoice (invoice_number, purchase_order_id, customer_id, invoice_date, total_amount)",
        "VALUES (#{invoiceNumber}, #{purchaseOrderId}, #{customerId}, #{invoiceDate}, #{totalAmount})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSalesInvoice(SalesInvoice salesInvoice);

    @Insert({
        "INSERT INTO sales_invoice_items (sales_invoice_id, product_id, quantity, unit_price)",
        "VALUES (#{salesInvoiceId}, #{productId}, #{quantity}, #{unitPrice})"
    })
    int insertSalesInvoiceItem(
        @Param("salesInvoiceId") Long salesInvoiceId,
        @Param("productId") Long productId,
        @Param("quantity") Integer quantity,
        @Param("unitPrice") java.math.BigDecimal unitPrice
    );

    @Select({
        "SELECT sii.id, si.invoice_number, sii.sales_invoice_id, sii.product_id, sii.quantity, sii.unit_price",
        "FROM sales_invoice_items sii",
        "JOIN sales_invoice si ON si.id = sii.sales_invoice_id",
        "WHERE si.purchase_order_id = #{purchaseOrderId}",
        "ORDER BY sii.id"
    })
    @Results(id = "salesInvoiceItemResultMap", value = {
        @Result(column = "id", property = "id", id = true),
        @Result(column = "invoice_number", property = "invoiceNumber"),
        @Result(column = "quantity", property = "quantity"),
        @Result(column = "unit_price", property = "unitPrice"),
        @Result(column = "product_id", property = "product", one = @One(select = "findProductById"))
    })
    List<SalesInvoiceItem> findSalesInvoiceItemsByPurchaseOrderId(@Param("purchaseOrderId") Long purchaseOrderId);

    @Select({
        "<script>",
        "SELECT po.id",
        "FROM purchase_order po",
        "WHERE (#{paymentStatus} = 'FULLY_PAID' AND (",
        "  (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id)",
        "  &lt;=",
        "  (SELECT COALESCE(SUM(sii.quantity), 0)",
        "   FROM sales_invoice_items sii",
        "   JOIN sales_invoice si ON si.id = sii.sales_invoice_id",
        "   WHERE si.purchase_order_id = po.id)",
        "))",
        "OR (#{paymentStatus} = 'ONGOING' AND (",
        "  (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id)",
        "  &gt;",
        "  (SELECT COALESCE(SUM(sii.quantity), 0)",
        "   FROM sales_invoice_items sii",
        "   JOIN sales_invoice si ON si.id = sii.sales_invoice_id",
        "   WHERE si.purchase_order_id = po.id)",
        "))",
        "</script>"
    })
    List<Long> findPurchaseOrderIdsByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    @Select({
        "SELECT po.id",
        "FROM purchase_order po",
        "JOIN customer c ON c.id = po.customer_id",
        "WHERE UPPER(c.name) LIKE CONCAT('%', UPPER(#{customer}), '%')",
        "ORDER BY po.id"
    })
    List<Long> findPurchaseOrderIdsByCustomer(@Param("customer") String customer);

    @Select({
        "<script>",
        "SELECT po.id AS purchase_order_id,",
        "CASE WHEN (",
        "  (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id)",
        "  &lt;=",
        "  (SELECT COALESCE(SUM(sii.quantity), 0)",
        "   FROM sales_invoice_items sii",
        "   JOIN sales_invoice si ON si.id = sii.sales_invoice_id",
        "   WHERE si.purchase_order_id = po.id)",
        ") THEN 'FULLY_PAID' ELSE 'ONGOING' END AS payment_status",
        "FROM purchase_order po",
        "WHERE po.id IN",
        "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "</script>"
    })
    List<PurchaseOrderPaymentStatusRow> findPaymentStatusByPurchaseOrderIds(@Param("ids") List<Long> ids);
}
