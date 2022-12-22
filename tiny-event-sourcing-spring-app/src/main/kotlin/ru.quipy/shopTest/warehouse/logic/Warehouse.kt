package ru.quipy.shopTest.warehouse.logic

import jdk.jfr.DataAmount
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.domain.Event
import ru.quipy.shopTest.warehouse.api.*
import java.lang.IllegalStateException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


class Warehouse: AggregateState<UUID, WarehouseAggregate>  {
    private lateinit var warehouseId: UUID
    var orders: MutableMap<UUID, Order> = mutableMapOf()
    var products: MutableMap<UUID, Product> = mutableMapOf()

    override fun getId() = warehouseId

    fun createNewWarehouse(id: UUID = UUID.randomUUID()): WarehouseCreatedEvent = WarehouseCreatedEvent(id)

    fun createNewOrder(id: UUID = UUID.randomUUID()): OrderCreatedEvent = OrderCreatedEvent(warehouseId, id)

    fun setDeliveryTime(id: UUID, time: LocalDateTime): OrderGotDeliveryEvent = OrderGotDeliveryEvent(warehouseId, id, time)

    fun addProductToWarehouse(id: UUID = UUID.randomUUID(), name: String, volume: BigDecimal, price: BigDecimal): ProductAddedToWhEvent = ProductAddedToWhEvent(warehouseId, id, name, price, volume)

    fun removeProductToWarehouse(id: UUID, volume: BigDecimal): ProductRemovedFromWhEvent {
        if (volume <= BigDecimal.ZERO) {
            throw IllegalStateException("Volume of removing products lower or equal zero")
        }
        val product = products[id] ?: throw IllegalArgumentException("No such product in warehouse : $id")
        return ProductRemovedFromWhEvent(warehouseId, id, volume)
    }

    fun changeOrderStatus(id: UUID, condition: OrderCondition): Event<WarehouseAggregate> {
        return when(condition) {
            OrderCondition.PAYED -> OrderPayedEvent(warehouseId, id, condition)
            OrderCondition.COMPLETED -> OrderCompletedEvent(warehouseId, id, condition)
            else -> OrderConditionChangedEvent(warehouseId, id, condition)
        }
    }

    fun bookProduct(productId: UUID, orderId: UUID, volume: BigDecimal): ProductBookedEvent {
        if (volume <= BigDecimal.ZERO) {
            throw IllegalStateException("Volume of booked products lower or equal zero")
        }
        val product = products[productId] ?: throw IllegalArgumentException("No such product in warehouse : $productId")
        if (product.volume - volume < BigDecimal.ZERO)
            throw IllegalStateException("Not enough products for booking : $productId has amount ${product.volume} while booking $volume")
        return ProductBookedEvent(warehouseId, productId, orderId, volume)
    }

    fun unbookProduct(productId: UUID, orderId: UUID, volume: BigDecimal): ProductUnbookedEvent {
        if (volume <= BigDecimal.ZERO) {
            throw IllegalStateException("Volume of unbooked products lower zero")
        }
        products[productId] ?: throw IllegalArgumentException("No such product in warehouse : $productId")
        return ProductUnbookedEvent(warehouseId, productId, orderId, volume)
    }

    @StateTransitionFunc
    fun createNewWarehouse(event: WarehouseCreatedEvent) {
        warehouseId = event.warehouseId
    }

    @StateTransitionFunc
    fun addProductToWarehouse(event: ProductAddedToWhEvent) {
        val product = products[event.productId]
        if (product != null) {
            product.increaseVolume(event.volume)
        }
        else {
            products[event.productId] = Product(event.name, event.price, event.volume)
        }
    }

    @StateTransitionFunc
    fun removeProductToWarehouse(event: ProductRemovedFromWhEvent) {
        val product = products[event.productId]!!
        product.decreaseVolume(event.volume)
        if (product.volume == BigDecimal.ZERO) {
            products.remove(event.productId)
        }
    }

    @StateTransitionFunc
    fun unbookProduct(event: ProductUnbookedEvent) {
        products[event.productId]!!.unbookedVolume(event.volume)
        orders[event.orderId]!!.decreaseVolumeOfProduct(event.productId, event.volume)
    }

    @StateTransitionFunc
    fun bookProduct(event: ProductBookedEvent) {
        products[event.productId]!!.bookedVolume(event.volume)
        orders[event.orderId]!!.increaseVolumeOfProduct(event.productId, event.volume)
    }

    @StateTransitionFunc
    fun createNewOrder(event: OrderCreatedEvent) {
        orders[event.orderId] = Order()
    }

    @StateTransitionFunc
    fun orderGotDelivery(event: OrderGotDeliveryEvent) {
        orders[event.orderId]!!.setTime(event.time)
    }

    @StateTransitionFunc
    fun changeOrderCondition(event: OrderConditionChangedEvent) {
        orders[event.orderId]!!.changeCondition(event.condition)
    }

    @StateTransitionFunc
    fun changeOrderConditionToCompleted(event: OrderCompletedEvent) {
        orders[event.orderId]!!.changeCondition(event.condition)
    }

    @StateTransitionFunc
    fun changeOrderConditionToPayed(event: OrderPayedEvent) {
        orders[event.orderId]!!.changeCondition(event.condition)
    }

}

data class Product(
        var name: String,
        var price: BigDecimal,
        var volume: BigDecimal,
        internal var booked_volume: BigDecimal = BigDecimal.ZERO ) {
    fun increaseVolume(volume: BigDecimal ) {
        this.volume += volume
    }

    fun decreaseVolume(volume: BigDecimal) {
        this.volume -= volume
    }

    fun bookedVolume(volume: BigDecimal) {
        this.booked_volume += volume
        this.volume -= volume
    }

    fun unbookedVolume(volume: BigDecimal) {
        this.booked_volume -= volume
        this.volume += volume
    }
}

data class Order(
        var deliveryTime: LocalDateTime? = null,
        internal var products: MutableMap<UUID, BigDecimal> = mutableMapOf(),
        internal var condition: OrderCondition = OrderCondition.CREATED,
        internal var price: BigDecimal = BigDecimal.ZERO
) {
    fun increaseVolumeOfProduct(productId: UUID, volume: BigDecimal) {
        this.products[productId] = if (this.products[productId] != null) this.products[productId]!! + volume else volume
    }

    fun decreaseVolumeOfProduct(productId: UUID, volume: BigDecimal) {
        if (this.products[productId] != null) {
            this.products[productId] = this.products[productId]!! - volume
            if (this.products[productId]!! == BigDecimal.ZERO)
                this.products.remove(productId)
        }
    }

    fun setTime(time: LocalDateTime) {
        this.deliveryTime = time
    }

    fun changeCondition(condition: OrderCondition) {
        this.condition = condition
    }
}