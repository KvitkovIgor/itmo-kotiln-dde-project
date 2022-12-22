
package ru.quipy

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.quipy.core.EventSourcingService
import ru.quipy.shopTest.warehouse.api.OrderCondition
import ru.quipy.shopTest.warehouse.api.WarehouseAggregate
import ru.quipy.shopTest.warehouse.logic.Warehouse
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SubscribersTest {
    companion object {
        private val orderId = UUID.randomUUID()
        private val productId = UUID.randomUUID()
        private val warehouseId = UUID.randomUUID()
        private val volume = BigDecimal(10)
        private val price = BigDecimal(200)
        private val name = "Intone Advisor"
    }

    @Autowired
    private lateinit var warehouseEsService: EventSourcingService<UUID, WarehouseAggregate, Warehouse>

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun init() {
        cleanDatabase()
    }

    fun cleanDatabase() {
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(orderId)), "orders")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(warehouseId)), "warehouse")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(orderId)), "snapshots")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(warehouseId)), "snapshots")
    }

    @Test
    fun deliverySubscriber() {
        warehouseEsService.create {
            it.createNewWarehouse(id = warehouseId)
        }

        warehouseEsService.update(warehouseId) {
            it.addProductToWarehouse(id = productId, name = name, price = price, volume = volume)
        }

        warehouseEsService.update(warehouseId) {
            it.createNewOrder(id = orderId)
        }


        warehouseEsService.update(warehouseId) {
            it.bookProduct(productId = productId, orderId = orderId, volume = BigDecimal(10))
        }

        warehouseEsService.update(warehouseId) {
            it.changeOrderStatus(orderId, OrderCondition.PAYED)
        }

        await.atMost(10, TimeUnit.SECONDS).until {
            warehouseEsService.getState(warehouseId)!!.orders[orderId]!!.condition == OrderCondition.DELIVERY_AWAIT
        }

        Assertions.assertNotNull(warehouseEsService.getState(warehouseId)!!.orders[orderId]!!.deliveryTime)
    }
}