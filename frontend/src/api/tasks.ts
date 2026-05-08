import { apiJson } from './client'
import type { CsvImportResponse, TaskRequest, TaskResponse } from './types'

export async function listTasks(): Promise<TaskResponse[]> {
  return apiJson<TaskResponse[]>('/api/tasks', { method: 'GET' })
}

export async function createTask(body: TaskRequest): Promise<TaskResponse> {
  return apiJson<TaskResponse>('/api/tasks', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function deleteTask(id: string): Promise<void> {
  await apiJson<void>(`/api/tasks/${id}`, { method: 'DELETE' })
}

export async function importTasksCsv(file: File): Promise<CsvImportResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return apiJson<CsvImportResponse>('/api/tasks/import', {
    method: 'POST',
    body: formData,
  })
}
