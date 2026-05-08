import { addToast } from '@heroui/react'
import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

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
      await register({
        email: email.trim().toLowerCase(),
        password,
      })
      navigate('/', { replace: true })
    } catch (unknownError) {
      const message =
        unknownError instanceof ApiError ? unknownError.message : 'Unable to create account. Please try again.'
      setErrorMessage(message)
      addToast({
        title: 'Registration failed',
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
          <h2 className="font-headline text-center text-xl font-bold text-on-surface">Create Account</h2>
          <form className={authFormStackClass} onSubmit={handleSubmit} noValidate>
            <div>
              <label className={authLabelClass} htmlFor="register-email">
                Email
              </label>
              <input
                id="register-email"
                name="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className={authFieldClass}
              />
            </div>
            <div>
              <label className={authLabelClass} htmlFor="register-password">
                Password
              </label>
              <input
                id="register-password"
                name="password"
                type="password"
                autoComplete="new-password"
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
              {isSubmitting ? 'Creating…' : 'Initialize Account'}
            </button>
          </form>
          <p className="mt-8 text-center">
            <Link
              to="/login"
              className="text-xs font-semibold uppercase tracking-widest text-tertiary hover:underline"
            >
              Back to login
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
