package ru.quipy.shopTest.warehouse.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

const val WAREHOUSE_CREATED = "WAREHOUSE_CREATED"
const val PRODUCT_ADD_TO_WH = "PRODUCT_ADD_TO_WH"
const val PRODUCT_RM_FROM_WH = "PRODUCT_RM_FROM_WH"
const val PRODUCT_BOOKED = "PRODUCT_BOOKED"
const val PRODUCT_UNBOOKED = "PRODUCT_UNBOOKED"
const val ORDER_CREATED = "ORDER_CREATED"
const val ORDER_GOT_DELIVERY = "ORDER_GOT_DELIVERY"
const val ORDER_CONDITION_CHANGED = "ORDER_CONDITION_CHANGED"
const val ORDER_PAYED = "ORDER_PAYED"
const val ORDER_COMPLETED = "ORDER_COMPLETED"

enum class OrderCondition {
    CREATED, PAYING, PAYED, DELIVERY_AWAIT, SHIPPING, COMPLETED, DISCARD
}

@DomainEvent(name = WAREHOUSE_CREATED)
data class WarehouseCreatedEvent(
        val warehouseId: UUID
) : Event<WarehouseAggregate>(
        name = WAREHOUSE_CREATED
)

@DomainEvent(name = PRODUCT_ADD_TO_WH)
data class ProductAddedToWhEvent(
        val warehouseId: UUID,
        val productId: UUID,
        val title: String,
        val price: BigDecimal,
        val volume: BigDecimal
) : Event<WarehouseAggregate>(
        name = PRODUCT_ADD_TO_WH
)

@DomainEvent(name = PRODUCT_RM_FROM_WH)
data class ProductRemovedFromWhEvent(
        val warehouseId: UUID,
        val productId: UUID,
        val volume: BigDecimal
) : Event<WarehouseAggregate>(
        name = PRODUCT_RM_FROM_WH
)

@DomainEvent(name = PRODUCT_BOOKED)
data class ProductBookedEvent(
        val warehouseId: UUID,
        val productId: UUID,
        val orderId: UUID,
        val volume: BigDecimal
) : Event<WarehouseAggregate>(
        name = PRODUCT_BOOKED
)

@DomainEvent(name = PRODUCT_UNBOOKED)
data class ProductUnbookedEvent(
        val warehouseId: UUID,
        val productId: UUID,
        val orderId: UUID,
        val volume: BigDecimal
) : Event<WarehouseAggregate>(
        name = PRODUCT_UNBOOKED
)

@DomainEvent(name = ORDER_CREATED)
data class OrderCreatedEvent(
        val warehouseId: UUID,
        val orderId: UUID
) : Event<WarehouseAggregate>(
        name = ORDER_CREATED
)

@DomainEvent(name = ORDER_GOT_DELIVERY)
data class OrderGotDeliveryEvent(
        val warehouseId: UUID,
        val orderId: UUID,
        val time: LocalDateTime
) : Event<WarehouseAggregate>(
        name = ORDER_GOT_DELIVERY
)

@DomainEvent(name = ORDER_CONDITION_CHANGED)
data class OrderConditionChangedEvent(
        val warehouseId: UUID,
        val orderId: UUID,
        val condition: OrderCondition
) : Event<WarehouseAggregate>(
        name = ORDER_CONDITION_CHANGED
)

@DomainEvent(name = ORDER_PAYED)
data class OrderPayedEvent(
        val warehouseId: UUID,
        val orderId: UUID,
        val condition: OrderCondition
) : Event<WarehouseAggregate>(
        name = ORDER_PAYED
)

@DomainEvent(name = ORDER_COMPLETED)
data class OrderCompletedEvent(
        val warehouseId: UUID,
        val orderId: UUID,
        val condition: OrderCondition
) : Event<WarehouseAggregate>(
        name = ORDER_COMPLETED
)


