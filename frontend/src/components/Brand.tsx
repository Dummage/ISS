export default function Brand({
  showSubtitle = true,
  align = 'center',
  size = 'hero',
}: {
  showSubtitle?: boolean
  align?: 'center' | 'left'
  size?: 'hero' | 'header'
}) {
  const alignClass = align === 'left' ? 'text-left' : 'text-center'
  const titleClass =
    size === 'header'
      ? 'font-headline text-2xl font-extrabold tracking-tighter text-primary'
      : 'font-headline text-4xl font-extrabold tracking-tighter text-primary'

  return (
    <div className={alignClass}>
      <h1 className={titleClass}>TaskManager</h1>
      {showSubtitle ? (
        <p className="mt-2 text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
          ENTER THE WORKSPACE
        </p>
      ) : null}
    </div>
  )
}
