import type { StudySet } from './studySet'

export interface Account {
  id: number
  email: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response
  try {
    response = await fetch(path, {
      ...options,
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    })
  } catch {
    throw new ApiError('Cannot reach the account API. Start it with `npm run api` and check the local MySQL settings.', 0)
  }

  const isJson = response.headers.get('content-type')?.includes('application/json') ?? false
  const body: unknown = isJson ? await response.json().catch(() => null) : null
  if (!response.ok) {
    const message =
      typeof body === 'object' && body !== null && 'error' in body &&
      typeof body.error === 'string'
        ? body.error
        : 'The account API returned an invalid response. Check that `npm run api` is running and MySQL is configured.'
    throw new ApiError(message, response.status)
  }

  if (!isJson || body === null) {
    throw new ApiError('The account API returned an invalid response. Check that `npm run api` is running.', response.status)
  }

  return body as T
}

export function getCurrentAccount() {
  return request<{ account: Account }>('/api/auth/me')
}

export function registerAccount(email: string, password: string) {
  return request<{ account: Account }>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function loginAccount(email: string, password: string) {
  return request<{ account: Account }>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function logoutAccount() {
  return request<{ success: true }>('/api/auth/logout', { method: 'POST' })
}

export async function loadStudySets() {
  const response = await request<{ studySets: StudySet[] }>('/api/study-sets')
  return response.studySets
}

export function saveStudySet(set: StudySet) {
  return request<{ studySet: StudySet }>('/api/study-sets', {
    method: 'POST',
    body: JSON.stringify(set),
  })
}

export function generateStudySetFromNotes(text: string, title: string) {
  return request<{ studySet: StudySet }>('/api/study-sets/generate', {
    method: 'POST',
    body: JSON.stringify({ text, title }),
  })
}
