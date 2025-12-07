package com.personal.adapters.outbound.persistence.adapters

import com.personal.adapters.outbound.persistence.entities.TaskEntity
import com.personal.domain.model.Task
import com.personal.domain.model.TaskId
import com.personal.domain.model.TasksPage
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
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

    override fun findAll(
        page: Int,
        size: Int,
    ): TasksPage {
        val query = Query().with(PageRequest.of(page, size))
        val tasks = mongoTemplate.find(query, TaskEntity::class.java)
        val totalElements = mongoTemplate.count(Query(), TaskEntity::class.java)

        return TasksPage.rehydrate(
            tasks = tasks.map { it.toDomain() },
            page = page,
            size = size,
            totalCount = totalElements,
        )
    }

    override fun updateTask(task: Task): Task? {
        val query =
            Query(
                Criteria
                    .where("taskId")
                    .`is`(task.taskId.uuid.toString())
            )

        val update = Update()
        update["completed"] = task.completed

        val options = FindAndModifyOptions.options().returnNew(true)
        val updated = mongoTemplate.findAndModify(query, update, options, TaskEntity::class.java)

        return updated?.toDomain()
    }
}
