package com.example.data.local

import com.example.model.VideoProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<VideoProject>> = dao.getAllProjects().map { entities ->
        entities.map { it.toProject() }
    }

    suspend fun getProjectById(id: Long): VideoProject? {
        return dao.getProjectById(id)?.toProject()
    }

    suspend fun saveProject(project: VideoProject): Long {
        val entity = project.toEntity()
        return dao.insertProject(entity)
    }

    suspend fun updateProject(project: VideoProject) {
        dao.updateProject(project.toEntity())
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }
}
