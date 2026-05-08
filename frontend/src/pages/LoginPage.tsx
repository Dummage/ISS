import { addToast } from '@heroui/react'
import { type FormEvent, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import Brand from '../components/Brand'
import { validateEmail, validatePassword } from '../validation'
import {
  authBlobBottomClass,
  authBlobTopClass,
  authCardClass,
  authFieldClass,
  authFormStackClass,
  authLabelClass,
  authPageShellClass,
  authSubmitButtonClass,
} from './authShared'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const redirectTarget =
    (location.state as { from?: string } | null)?.from && (location.state as { from?: string }).from !== ''
      ? (location.state as { from: string }).from
      : '/'

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage(null)

    const emailErr = validateEmail(email)
    const passwordErr = validatePassword(password)
    if (emailErr || passwordErr) {
      setErrorMessage(emailErr ?? passwordErr ?? null)
      return
    }

    setIsSubmitting(true)
    try {
      await login({
        email: email.trim().toLowerCase(),
        password,
      })
      navigate(redirectTarget, { replace: true })
    } catch (unknownError) {
      const message =
        unknownError instanceof ApiError ? unknownError.message : 'Unable to sign in. Please try again.'
      setErrorMessage(message)
      addToast({
        title: 'Login failed',
        description: message,
        color: 'danger',
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className={authPageShellClass}>
      <div className={authBlobTopClass} aria-hidden />
      <div className={authBlobBottomClass} aria-hidden />
      <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4 py-16">
        <Brand />
        <div className={`${authCardClass} mt-10`}>
          <h2 className="font-headline text-center text-xl font-bold text-on-surface">Welcome back</h2>
          <form className={authFormStackClass} onSubmit={handleSubmit} noValidate>
            <div>
              <label className={authLabelClass} htmlFor="login-email">
                Email
              </label>
              <input
                id="login-email"
                name="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className={authFieldClass}
              />
            </div>
            <div>
              <label className={authLabelClass} htmlFor="login-password">
                Password
              </label>
              <input
                id="login-password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className={authFieldClass}
              />
            </div>
            {errorMessage ? (
              <p className="text-center text-sm text-error" role="alert">
                {errorMessage}
              </p>
            ) : null}
            <button type="submit" className={authSubmitButtonClass} disabled={isSubmitting}>
              {isSubmitting ? 'Signing in…' : 'Initialize Session'}
            </button>
          </form>
          <p className="mt-8 text-center">
            <Link
              to="/register"
              className="text-xs font-semibold uppercase tracking-widest text-tertiary hover:underline"
            >
              Register Account
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
