package ru.quipy.shopTest.delivery.subscribers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import ru.quipy.core.EventSourcingService
import ru.quipy.shopTest.delivery.api.DeliveryAggregate
import ru.quipy.shopTest.delivery.logic.Delivery
import ru.quipy.shopTest.warehouse.api.OrderPayedEvent
import ru.quipy.shopTest.warehouse.api.WarehouseAggregate
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct


@Component
class WarehouseSubscriber(
        private val subscriptionsManager: AggregateSubscriptionsManager,
        private val deliveryEsService: EventSourcingService<UUID, DeliveryAggregate, Delivery>
) {
    private val logger: Logger = LoggerFactory.getLogger(WarehouseSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(
                WarehouseAggregate::class,
                "warehouse::order-payed"
        ) {
            `when`(OrderPayedEvent::class) { event ->
                    deliveryEsService.create {
                        it.createNewDelivery(
                                orderId = event.orderId,
                                warehouseId = event.warehouseId
                        )
                    }
            }
        }
    }
}