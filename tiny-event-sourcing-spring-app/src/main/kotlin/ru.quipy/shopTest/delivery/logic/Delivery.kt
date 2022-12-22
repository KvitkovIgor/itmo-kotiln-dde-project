package ru.quipy.shopTest.delivery.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.shopTest.delivery.api.DeliveryAggregate
import ru.quipy.shopTest.delivery.api.DeliveryCreatedEvent
import java.time.LocalDateTime
import java.util.*


class Delivery : AggregateState<UUID, DeliveryAggregate> {
    private lateinit var deliveryId: UUID
    private lateinit var orderId: UUID

    override fun getId() = deliveryId

    fun createNewDelivery(id: UUID = UUID.randomUUID(), orderId: UUID, warehouseId: UUID): DeliveryCreatedEvent {
        return DeliveryCreatedEvent(deliveryId = id, orderId = orderId, warehouseId = warehouseId, time = LocalDateTime.now())
    }

    @StateTransitionFunc
    fun createNewDelivery(event: DeliveryCreatedEvent) {
        deliveryId = event.deliveryId
        orderId = event.orderId
    }
}