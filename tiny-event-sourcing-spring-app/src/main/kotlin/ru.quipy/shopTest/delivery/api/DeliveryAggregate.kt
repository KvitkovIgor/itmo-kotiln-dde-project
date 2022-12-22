package ru.quipy.shopTest.delivery.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "delivery")
class DeliveryAggregate: Aggregate