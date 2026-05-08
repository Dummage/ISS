import {
  addToast,
  Button,
  Input,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Textarea,
} from '@heroui/react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { ApiError } from '../api/types'
import { createTask } from '../api/tasks'
import { tasksQueryKey } from '../queryKeys'
import { validateTaskDescription, validateTaskTitle } from '../validation'

export default function CreateTaskDialog({
  isOpen,
  onClose,
}: {
  isOpen: boolean
  onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    if (!isOpen) {
      setTitle('')
      setDescription('')
    }
  }, [isOpen])

  const mutation = useMutation({
    mutationFn: () =>
      createTask({
        title: title.trim(),
        description: description.trim().length > 0 ? description.trim() : undefined,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: tasksQueryKey })
      addToast({
        title: 'Task created.',
        color: 'success',
      })
      onClose()
    },
    onError: (unknownError: unknown) => {
      const message =
        unknownError instanceof ApiError ? unknownError.message : 'Could not create the task.'
      addToast({
        title: 'Error',
        description: message,
        color: 'danger',
      })
    },
  })

  const titleError = validateTaskTitle(title)
  const descriptionError = validateTaskDescription(description)
  const canSubmit = titleError === null && descriptionError === null && title.trim().length > 0

  function handleOpenChange(next: boolean) {
    if (!next) {
      onClose()
    }
  }

  function handleSubmit() {
    const tErr = validateTaskTitle(title)
    const dErr = validateTaskDescription(description)
    if (tErr || dErr || title.trim().length === 0) {
      return
    }
    mutation.mutate()
  }

  return (
    <Modal isOpen={isOpen} onOpenChange={handleOpenChange} placement="center">
      <ModalContent>
        <ModalHeader className="font-headline text-on-surface">New Task</ModalHeader>
        <ModalBody className="gap-4">
          <Input
            label="Title"
            labelPlacement="outside"
            maxLength={500}
            value={title}
            onValueChange={setTitle}
            classNames={{
              label: 'text-xs font-semibold uppercase tracking-widest text-on-surface-variant',
              input: 'text-on-surface',
              inputWrapper: 'bg-surface-container-highest',
            }}
            isInvalid={title.length > 0 && validateTaskTitle(title) !== null}
            errorMessage={title.length > 0 ? validateTaskTitle(title) : undefined}
          />
          <Textarea
            label="Description"
            labelPlacement="outside"
            maxLength={10000}
            minRows={4}
            value={description}
            onValueChange={setDescription}
            classNames={{
              label: 'text-xs font-semibold uppercase tracking-widest text-on-surface-variant',
              input: 'text-on-surface',
              inputWrapper: 'bg-surface-container-highest',
            }}
            isInvalid={description.length > 0 && validateTaskDescription(description) !== null}
            errorMessage={description.length > 0 ? validateTaskDescription(description) : undefined}
          />
        </ModalBody>
        <ModalFooter>
          <Button variant="light" onPress={onClose}>
            Cancel
          </Button>
          <Button
            color="primary"
            onPress={handleSubmit}
            isDisabled={!canSubmit}
            isLoading={mutation.isPending}
          >
            Create
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  )
}
