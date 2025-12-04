package com.personal.adapters.outbound.persistence.adapters

import com.personal.adapters.outbound.persistence.entities.TaskEntity
import com.personal.domain.model.Task
import com.personal.domain.model.TaskId
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TaskRepositoryAdapter(
    private val mongoTemplate: MongoTemplate,
) : TaskRepositoryPort {
    override fun save(task: Task): Task {
        val entity = TaskEntity.fromDomain(task)
        val savedEntity = mongoTemplate.save(entity)
        return savedEntity.toDomain()
    }

    override fun saveAll(tasks: List<Task>): List<Task> {
        val entities = tasks.map { TaskEntity.fromDomain(it) }
        val savedEntities = mongoTemplate.insertAll(entities)
        return savedEntities.map { it.toDomain() }
    }

    override fun findById(taskId: TaskId): Task? {
        val query = Query(Criteria.where("taskId").`is`(taskId.uuid.toString()))
        return mongoTemplate.findOne(query, TaskEntity::class.java)?.toDomain()
    }
}
