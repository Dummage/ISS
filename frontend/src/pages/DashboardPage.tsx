import { addToast, Button } from '@heroui/react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { type ChangeEvent, useEffect, useRef, useState } from 'react'
import { ApiError, type TaskResponse } from '../api/types'
import { importTasksCsv, listTasks } from '../api/tasks'
import { useAuth } from '../auth/AuthContext'
import Brand from '../components/Brand'
import ConfirmDeleteDialog from '../components/ConfirmDeleteDialog'
import CreateTaskDialog from '../components/CreateTaskDialog'
import TaskCard from '../components/TaskCard'
import { tasksQueryKey } from '../queryKeys'

const headerBarClass = 'fixed left-0 right-0 top-0 z-20 flex h-16 items-center justify-between bg-[#171b2c] px-4'

const headerActionsClass = 'flex items-center gap-2'

const importButtonClass =
  'font-semibold text-on-surface-variant hover:bg-surface-bright min-h-10 rounded-lg px-4'

const createButtonClass =
  'min-h-10 rounded-lg bg-primary-container px-4 font-semibold text-on-primary-container'

export default function DashboardPage() {
  const { logout } = useAuth()
  const queryClient = useQueryClient()
  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const errorToastSentRef = useRef(false)

  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [taskPendingDelete, setTaskPendingDelete] = useState<TaskResponse | null>(null)

  const tasksQuery = useQuery({
    queryKey: tasksQueryKey,
    queryFn: listTasks,
  })

  const importMutation = useMutation({
    mutationFn: (file: File) => importTasksCsv(file),
    onSuccess: async (data) => {
      await queryClient.invalidateQueries({ queryKey: tasksQueryKey })
      addToast({
        title: 'Import complete',
        description: `Imported ${data.imported} tasks (${data.skipped} skipped)`,
        color: 'success',
      })
    },
    onError: (unknownError: unknown) => {
      const message =
        unknownError instanceof ApiError ? unknownError.message : 'Could not import the CSV file.'
      addToast({
        title: 'Import failed',
        description: message,
        color: 'danger',
      })
    },
  })

  useEffect(() => {
    if (tasksQuery.isError && !errorToastSentRef.current) {
      errorToastSentRef.current = true
      const message =
        tasksQuery.error instanceof ApiError
          ? tasksQuery.error.message
          : 'Something went wrong while loading tasks.'
      addToast({
        title: 'Could not load tasks',
        description: message,
        color: 'danger',
      })
    }
    if (!tasksQuery.isError) {
      errorToastSentRef.current = false
    }
  }, [tasksQuery.isError, tasksQuery.error])

  function handleLogoutPress() {
    void logout()
  }

  function handleCreatePress() {
    setIsCreateOpen(true)
  }

  function handleImportClick() {
    fileInputRef.current?.click()
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (file) {
      importMutation.mutate(file)
    }
  }

  function handleRequestDelete(task: TaskResponse) {
    setTaskPendingDelete(task)
  }

  function handleCloseDeleteDialog() {
    setTaskPendingDelete(null)
  }

  function handleRetryLoad() {
    void tasksQuery.refetch()
  }

  const taskList = tasksQuery.data ?? []

  return (
    <div className="min-h-screen bg-background font-body text-on-surface">
      <header className={headerBarClass}>
        <Brand showSubtitle={false} align="left" size="header" />
        <div className={headerActionsClass}>
          <Button className={createButtonClass} onPress={handleCreatePress}>
            Create Task
          </Button>
          <Button className={importButtonClass} variant="light" onPress={handleImportClick}>
            Import CSV
          </Button>
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv"
            className="hidden"
            aria-hidden
            onChange={handleFileChange}
          />
          <Button isIconOnly variant="light" onPress={handleLogoutPress} aria-label="Log out">
            <span className="material-symbols-outlined">logout</span>
          </Button>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-4 pb-20 pt-24">
        <h2 className="font-headline text-4xl font-extrabold tracking-tight text-primary">Your Tasks</h2>

        {tasksQuery.isLoading ? (
          <p className="mt-10 text-on-surface-variant">Loading tasks...</p>
        ) : null}

        {tasksQuery.isError ? (
          <div className="mt-10 rounded-lg bg-surface-container-low p-6 text-center">
            <p className="text-on-surface-variant">We couldn&apos;t refresh your task list.</p>
            <Button className="mt-4" color="primary" variant="flat" onPress={handleRetryLoad}>
              Retry
            </Button>
          </div>
        ) : null}

        {tasksQuery.isSuccess && taskList.length === 0 ? (
          <div className="mt-10 rounded-lg bg-surface-container-low p-10 text-center text-on-surface-variant">
            No tasks yet. Create your first one.
          </div>
        ) : null}

        {tasksQuery.isSuccess && taskList.length > 0 ? (
          <div className="mt-10 space-y-6">
            {taskList.map((task) => (
              <TaskCard key={task.id} task={task} onRequestDelete={handleRequestDelete} />
            ))}
          </div>
        ) : null}
      </main>

      <CreateTaskDialog isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)} />
      <ConfirmDeleteDialog
        isOpen={taskPendingDelete !== null}
        taskId={taskPendingDelete?.id ?? null}
        onClose={handleCloseDeleteDialog}
      />
    </div>
  )
}
