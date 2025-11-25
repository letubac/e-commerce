INSERT INTO order_items (
    order_id,
    product_id,
    product_name,
    product_sku,
    quantity,
    price,
    total,
    created_at,
    updated_at
)
VALUES
/*%for orderItem in orderItems */
    (
        /*orderItem.orderId*/,
        /*orderItem.productId*/,
        /*orderItem.productName*/,
        /*orderItem.productSku*/,
        /*orderItem.quantity*/,
        /*orderItem.price*/,
        /*orderItem.total*/,
        /*orderItem.createdAt*/,
        /*orderItem.updatedAt*/
    )
    /*%if orderItem_has_next */,/*%end */
/*%end */