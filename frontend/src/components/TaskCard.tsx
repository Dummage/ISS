import { Button } from '@heroui/react'
import type { TaskResponse } from '../api/types'

function formatTaskTimestamp(iso: string): string {
  const date = new Date(iso)
  const formatter = new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  })
  return formatter.format(date)
}

const cardClass =
  'group relative rounded-lg bg-surface-container-low p-6 transition-colors hover:bg-surface-bright'

export default function TaskCard({
  task,
  onRequestDelete,
}: {
  task: TaskResponse
  onRequestDelete: (task: TaskResponse) => void
}) {
  function handleDeleteClick() {
    onRequestDelete(task)
  }

  const descriptionText = task.description ?? ''

  return (
    <article className={cardClass}>
      <div className="pr-14">
        <h3 className="text-lg font-bold text-on-surface group-hover:text-primary">{task.title}</h3>
        {descriptionText.length > 0 ? (
          <p className="mt-2 whitespace-pre-wrap text-sm text-on-surface-variant">{descriptionText}</p>
        ) : null}
        <p className="mt-3 flex items-center gap-1 text-sm text-on-surface-variant">
          <span className="material-symbols-outlined text-base">calendar_month</span>
          <span>{formatTaskTimestamp(task.createdAt)}</span>
        </p>
      </div>
      <div className="absolute right-4 top-4">
        <Button
          isIconOnly
          type="button"
          variant="light"
          onPress={handleDeleteClick}
          className="text-on-surface-variant"
          aria-label="Delete task"
        >
          <span className="material-symbols-outlined">delete</span>
        </Button>
      </div>
    </article>
  )
}
