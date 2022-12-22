package ru.quipy

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
import ru.quipy.shopTest.warehouse.api.WarehouseAggregate
import ru.quipy.shopTest.warehouse.logic.Warehouse
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookProductTest {
    companion object {
        private val orderId = UUID.randomUUID()
        private val productId = UUID.randomUUID()
        private val warehouseId = UUID.randomUUID()
        private val volume = BigDecimal(9)
        private val price = BigDecimal(100)
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
    fun createWarehouse() {
        warehouseEsService.create {
            it.createNewWarehouse(id = warehouseId)
        }

        val state = warehouseEsService.getState(warehouseId)!!
        Assertions.assertEquals(state.getId(), warehouseId)
    }
    @Test
    fun addProduct() {
        warehouseEsService.create {
            it.createNewWarehouse(id = warehouseId)
        }

        warehouseEsService.update(warehouseId) {
            it.addProductToWarehouse(id = productId, name = name, price = price, volume = volume)
        }

        val state = warehouseEsService.getState(warehouseId)!!
        Assertions.assertNotNull(state.products[productId])
        Assertions.assertEquals(volume, state.products[productId]!!.volume)
    }

    @Test
    fun createOrder() {
        warehouseEsService.create {
            it.createNewWarehouse(id = warehouseId)
        }

        warehouseEsService.update(warehouseId) {
            it.createNewOrder(id = orderId)
        }

        val state = warehouseEsService.getState(warehouseId)!!
        Assertions.assertNotNull(state.orders[orderId])
    }

    @Test
    fun addProductToOrder() {
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
            it.bookProduct(productId = productId, orderId = orderId, volume = BigDecimal(6))
        }

        val state = warehouseEsService.getState(warehouseId)!!
        Assertions.assertEquals(BigDecimal(6), state.orders[orderId]!!.products[productId])
        Assertions.assertEquals(BigDecimal(3), state.products[productId]!!.volume)
        Assertions.assertEquals(BigDecimal(6), state.products[productId]!!.booked_volume)
    }

}