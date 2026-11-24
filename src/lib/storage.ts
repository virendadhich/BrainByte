const ACTIVE_KEY = 'brainbyte:active-set-id'

export function getActiveSetId(): string | null {
  return localStorage.getItem(ACTIVE_KEY)
}

export function setActiveSetId(id: string) {
  localStorage.setItem(ACTIVE_KEY, id)
}
