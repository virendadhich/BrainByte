import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { Layers, ListChecks, LogOut, Plus } from 'lucide-react'
import LiquidGlassPanel from '../components/LiquidGlassPanel'
import QuizPlayer from '../components/QuizPlayer'
import FlashcardDeck from '../components/FlashcardDeck'
import { demoSets } from '../lib/demoSets'
import { loadStudySets } from '../lib/api'
import { useAuth } from '../lib/AuthContext'
import { getActiveSetId, setActiveSetId } from '../lib/storage'
import type { StudySet } from '../lib/studySet'

type Mode = 'quiz' | 'flashcards'

export default function QuizzesPage() {
  const { account, loading: authLoading, error: authError, login, register, logout } = useAuth()
  const [userSets, setUserSets] = useState<StudySet[]>([])
  const [activeId, setActiveId] = useState<string>('demo-science')
  const [mode, setMode] = useState<Mode>('quiz')
  const [setsLoading, setSetsLoading] = useState(false)
  const [setsError, setSetsError] = useState<string | null>(null)
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [authBusy, setAuthBusy] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    if (authLoading) return
    if (!account) {
      setUserSets([])
      setSetsLoading(false)
      setSetsError(null)
      return
    }

    setSetsLoading(true)
    setSetsError(null)
    loadStudySets()
      .then((sets) => {
        if (!active) return
        setUserSets(sets)
        const stored = getActiveSetId()
        if (stored && [...sets, ...demoSets].some((set) => set.id === stored)) {
          setActiveId(stored)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setSetsError(error instanceof Error ? error.message : 'Unable to load your study sets.')
        }
      })
      .finally(() => {
        if (active) setSetsLoading(false)
      })

    return () => {
      active = false
    }
  }, [authLoading, account?.id])

  const allSets = [...userSets, ...demoSets]
  const activeSet = allSets.find((s) => s.id === activeId) || allSets[0]

  const submitAuth = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAuthBusy(true)
    setFormError(null)
    try {
      if (authMode === 'login') await login(email, password)
      else await register(email, password)
      setPassword('')
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Unable to authenticate.')
    } finally {
      setAuthBusy(false)
    }
  }

  const handleLogout = async () => {
    setFormError(null)
    try {
      await logout()
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Unable to sign out.')
    }
  }

  const selectSet = (id: string) => {
    setActiveId(id)
    setActiveSetId(id)
    setMode('quiz')
  }

  return (
    <section className="min-h-screen w-full px-4 pb-24 pt-32 sm:px-6 sm:pt-40">
      <div className="mx-auto grid max-w-6xl grid-cols-1 gap-6 lg:grid-cols-[280px_1fr]">
        <aside className="lg:sticky lg:top-28 lg:self-start">
          <LiquidGlassPanel className="mb-6 p-4">
            {authLoading ? (
              <p className="text-xs text-silver">Checking account…</p>
            ) : account ? (
              <div>
                <p className="truncate text-sm text-cream">{account.email}</p>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="mt-3 flex items-center gap-2 text-xs text-silver hover:text-cream"
                >
                  <LogOut size={13} /> Sign out
                </button>
              </div>
            ) : (
              <form className="space-y-3" onSubmit={submitAuth}>
                <p className="text-sm text-cream">
                  {authMode === 'login' ? 'Sign in to your account' : 'Create your account'}
                </p>
                {authError && <p className="text-xs text-[#c98a6b]">{authError}</p>}
                <input
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="Email"
                  aria-label="Email"
                  className="liquid-glass w-full rounded-full px-4 py-2.5 text-sm text-cream placeholder:text-gray focus:outline-none"
                />
                <input
                  type="password"
                  autoComplete={authMode === 'login' ? 'current-password' : 'new-password'}
                  minLength={10}
                  maxLength={72}
                  required
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Password (10+ characters)"
                  aria-label="Password"
                  className="liquid-glass w-full rounded-full px-4 py-2.5 text-sm text-cream placeholder:text-gray focus:outline-none"
                />
                {formError && <p className="text-xs text-[#c98a6b]">{formError}</p>}
                <button
                  type="submit"
                  disabled={authBusy}
                  className="w-full rounded-full bg-ivory px-4 py-2.5 text-sm font-medium text-void disabled:opacity-50"
                >
                  {authBusy ? 'Please wait…' : authMode === 'login' ? 'Sign in' : 'Create account'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setAuthMode(authMode === 'login' ? 'register' : 'login')
                    setFormError(null)
                  }}
                  className="w-full text-xs text-silver hover:text-cream"
                >
                  {authMode === 'login' ? 'Need an account? Sign up' : 'Already registered? Sign in'}
                </button>
              </form>
            )}
          </LiquidGlassPanel>

          <div className="mb-4 flex items-center justify-between">
            <p className="text-xs uppercase tracking-[0.14em] text-silver">Your sets</p>
            <Link
              to="/create"
              className="flex items-center gap-1 text-xs text-cream hover:text-ivory"
            >
              <Plus size={13} /> New
            </Link>
          </div>

          {setsLoading && <p className="mb-4 text-xs text-silver">Loading your study sets…</p>}
          {setsError && <p className="mb-4 text-xs text-[#c98a6b]">{setsError}</p>}

          {account && !setsLoading && userSets.length === 0 && (
            <LiquidGlassPanel className="mb-4 p-4 text-xs text-silver">
              You haven't created a set yet.{' '}
              <Link to="/create" className="text-cream underline underline-offset-4">
                Upload some notes
              </Link>{' '}
              to get started.
            </LiquidGlassPanel>
          )}

          <ul className="mb-6 space-y-2">
            {userSets.map((s) => (
              <SetRow key={s.id} set={s} active={s.id === activeId} onClick={() => selectSet(s.id)} />
            ))}
          </ul>

          <p className="mb-4 text-xs uppercase tracking-[0.14em] text-silver">Try a free set</p>
          <ul className="space-y-2">
            {demoSets.map((s) => (
              <SetRow key={s.id} set={s} active={s.id === activeId} onClick={() => selectSet(s.id)} />
            ))}
          </ul>
        </aside>

        <div>
          {activeSet && (
            <motion.div
              key={activeSet.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
            >
              <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                  <p className="text-xs uppercase tracking-[0.14em] text-bronze">Study set</p>
                  <h1 className="mt-2 font-display text-3xl text-cream sm:text-4xl">
                    {activeSet.title}
                  </h1>
                </div>

                <div className="flex gap-2 rounded-full border border-ivory/10 bg-white/[0.02] p-1">
                  <ModeButton active={mode === 'quiz'} onClick={() => setMode('quiz')} icon={<ListChecks size={14} />}>
                    Quiz
                  </ModeButton>
                  <ModeButton
                    active={mode === 'flashcards'}
                    onClick={() => setMode('flashcards')}
                    icon={<Layers size={14} />}
                  >
                    Flashcards
                  </ModeButton>
                </div>
              </div>

              <LiquidGlassPanel className="p-6 sm:p-8">
                {mode === 'quiz' ? (
                  <QuizPlayer questions={activeSet.questions} />
                ) : (
                  <FlashcardDeck cards={activeSet.flashcards} />
                )}
              </LiquidGlassPanel>
            </motion.div>
          )}
        </div>
      </div>
    </section>
  )
}

function SetRow({ set, active, onClick }: { set: StudySet; active: boolean; onClick: () => void }) {
  return (
    <li>
      <button
        onClick={onClick}
        className={`w-full rounded-2xl border px-4 py-3 text-left text-sm transition-colors ${
          active
            ? 'border-cream/30 bg-cream/[0.06] text-cream'
            : 'border-ivory/10 bg-white/[0.01] text-silver hover:border-ivory/20'
        }`}
      >
        <p className="truncate">{set.title}</p>
        <p className="mt-0.5 truncate text-xs text-gray">
          {set.questions.length} questions · {set.flashcards.length} cards
        </p>
      </button>
    </li>
  )
}

function ModeButton({
  active,
  onClick,
  icon,
  children,
}: {
  active: boolean
  onClick: () => void
  icon: ReactNode
  children: ReactNode
}) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-1.5 rounded-full px-4 py-2 text-xs font-medium transition-colors ${
        active ? 'bg-ivory text-void' : 'text-silver hover:text-cream'
      }`}
    >
      {icon}
      {children}
    </button>
  )
}
