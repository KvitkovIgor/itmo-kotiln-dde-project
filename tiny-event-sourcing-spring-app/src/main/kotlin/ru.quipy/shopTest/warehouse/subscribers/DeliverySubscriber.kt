package ru.quipy.shopTest.warehouse.subscribers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import ru.quipy.core.EventSourcingService
import ru.quipy.shopTest.delivery.api.DeliveryAggregate
import ru.quipy.shopTest.delivery.api.DeliveryCreatedEvent
import ru.quipy.shopTest.warehouse.api.OrderCondition
import ru.quipy.shopTest.warehouse.api.WarehouseAggregate
import ru.quipy.shopTest.warehouse.logic.Warehouse
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct


@Component
class DeliverySubscriber(
        private val subscriptionsManager: AggregateSubscriptionsManager,
        private val warehouseEsService: EventSourcingService<UUID, WarehouseAggregate, Warehouse>
) {
    private val logger: Logger = LoggerFactory.getLogger(DeliverySubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(
                DeliveryAggregate::class,
                "delivery::create-delivery"
        ) {
            `when`(DeliveryCreatedEvent::class) { event ->
                     warehouseEsService.update(event.warehouseId) {
                        it.changeOrderStatus(
                                event.orderId,
                                OrderCondition.DELIVERY_AWAIT
                        )
                    }
                    warehouseEsService.update(event.warehouseId) {
                        it.setDeliveryTime(id = event.orderId, time = event.time)
                    }
            }
        }
    }
}