UPDATE order_items SET
    order_id = /* orderItem.orderId */,
    product_id = /* orderItem.productId */,
    product_name = /* orderItem.productName */,
    product_sku = /* orderItem.productSku */,
    quantity = /* orderItem.quantity */,
    price = /* orderItem.price */,
    total = /* orderItem.total */
WHERE id = /* orderItem.id */