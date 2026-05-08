const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateEmail(value: string): string | null {
  const trimmed = value.trim()
  if (trimmed.length === 0) {
    return 'Email is required.'
  }
  if (!EMAIL_PATTERN.test(trimmed)) {
    return 'Enter a valid email address.'
  }
  return null
}

export function validatePassword(value: string): string | null {
  if (value.length === 0) {
    return 'Password is required.'
  }
  if (value.length < 8 || value.length > 128) {
    return 'Password must be between 8 and 128 characters.'
  }
  return null
}

export function validateTaskTitle(value: string): string | null {
  const trimmed = value.trim()
  if (trimmed.length === 0) {
    return 'Title is required.'
  }
  if (trimmed.length > 500) {
    return 'Title must be at most 500 characters.'
  }
  return null
}

export function validateTaskDescription(value: string): string | null {
  if (value.length > 10000) {
    return 'Description must be at most 10000 characters.'
  }
  return null
}
