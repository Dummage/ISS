import { addToast, Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@heroui/react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/types'
import { deleteTask } from '../api/tasks'
import { tasksQueryKey } from '../queryKeys'

export default function ConfirmDeleteDialog({
  isOpen,
  taskId,
  onClose,
}: {
  isOpen: boolean
  taskId: string | null
  onClose: () => void
}) {
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: tasksQueryKey })
      addToast({
        title: 'Task deleted.',
        color: 'success',
      })
      onClose()
    },
    onError: (unknownError: unknown) => {
      const message =
        unknownError instanceof ApiError ? unknownError.message : 'Could not delete the task.'
      addToast({
        title: 'Error',
        description: message,
        color: 'danger',
      })
    },
  })

  function handleOpenChange(next: boolean) {
    if (!next) {
      onClose()
    }
  }

  function handleDelete() {
    if (taskId) {
      mutation.mutate(taskId)
    }
  }

  return (
    <Modal isOpen={isOpen} onOpenChange={handleOpenChange} placement="center">
      <ModalContent>
        <ModalHeader className="font-headline text-on-surface">Delete task</ModalHeader>
        <ModalBody>
          <p className="text-sm text-on-surface-variant">
            This action cannot be undone. The task will be permanently removed.
          </p>
        </ModalBody>
        <ModalFooter>
          <Button variant="light" onPress={onClose}>
            Cancel
          </Button>
          <Button color="danger" onPress={handleDelete} isLoading={mutation.isPending}>
            Delete
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  )
}
