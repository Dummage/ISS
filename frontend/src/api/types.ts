export type UserResponse = {
  id: string
  email: string
}

export type RegisterRequest = {
  email: string
  password: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type TaskResponse = {
  id: string
  title: string
  description: string | null
  createdAt: string
}

export type TaskRequest = {
  title: string
  description?: string
}

export type CsvImportResponse = {
  imported: number
  skipped: number
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }

  static async fromResponse(response: Response): Promise<ApiError> {
    let message = response.statusText || 'Request failed'
    const contentType = response.headers.get('content-type') ?? ''

    try {
      if (contentType.includes('application/json')) {
        const body: unknown = await response.json()
        const extracted = extractMessageFromJsonBody(body)
        if (extracted) {
          message = extracted
        }
      } else {
        const text = await response.text()
        if (text.trim().length > 0) {
          message = text
        }
      }
    } catch {
      /* keep default message */
    }

    return new ApiError(response.status, message)
  }
}

function extractMessageFromJsonBody(body: unknown): string | null {
  if (body === null || typeof body !== 'object') {
    return null
  }

  const record = body as Record<string, unknown>

  if (typeof record.message === 'string') {
    return record.message
  }

  const values = Object.values(record).filter((v): v is string => typeof v === 'string')
  if (values.length > 0) {
    return values.join(' ')
  }

  return null
}
