package ru.quipy.shopTest.delivery.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.time.LocalDateTime
import java.util.*

const val DELIVERY_CREATED = "DELIVERY_CREATED"

@DomainEvent(name = DELIVERY_CREATED)
data class DeliveryCreatedEvent(
        val deliveryId: UUID,
        val orderId: UUID,
        val warehouseId: UUID,
        val time: LocalDateTime
) : Event<DeliveryAggregate>(
        name = DELIVERY_CREATED
)