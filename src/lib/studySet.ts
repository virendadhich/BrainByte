export interface Flashcard {
  id: string
  front: string
  back: string
}

export interface QuizQuestion {
  id: string
  prompt: string
  options: string[]
  correctIndex: number
}

export interface StudySet {
  id: string
  title: string
  sourceExcerpt: string
  createdAt: number
  flashcards: Flashcard[]
  questions: QuizQuestion[]
}
