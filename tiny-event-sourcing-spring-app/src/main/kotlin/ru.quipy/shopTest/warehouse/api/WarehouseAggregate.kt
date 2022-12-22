package ru.quipy.shopTest.warehouse.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "warehouse")
class WarehouseAggregate: Aggregate