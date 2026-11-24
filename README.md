# BrainByte

A cinematic, 3D quiz-platform landing page. React 18 + Vite + TypeScript + Tailwind CSS,
with Three.js / React Three Fiber for the liquid-metal scene and Framer Motion for the
scroll-driven choreography.

## Run it

Requires Node.js 20+ and MySQL 8+.

```bash
npm install
```

Create a database and an application user in MySQL:

```sql
CREATE DATABASE brainbyte CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'brainbyte'@'localhost' IDENTIFIED BY 'use-a-strong-local-password';
GRANT ALL PRIVILEGES ON brainbyte.* TO 'brainbyte'@'localhost';
```

Copy `.env.example` to `.env` and set `MYSQL_USER`, `MYSQL_PASSWORD`, and
`GEMINI_API_KEY` from [Google AI Studio](https://aistudio.google.com/apikey).
The API creates its tables on startup. `GEMINI_MODEL` defaults to
`gemini-3.5-flash-lite`. Keep `.env` private and never commit it.

Run the API and frontend in separate terminals:

```bash
npm run api
npm run dev
```

Open the Vite URL (typically `http://localhost:5173`). Vite proxies `/api`
requests to the API on port 3001.

If sign-in reports that the account API is unreachable, make sure the API
terminal is running without a startup error. The API requires valid
`MYSQL_USER`, `MYSQL_PASSWORD`, and database settings in `.env`; MySQL itself
being running is not enough.

To build for production:

```bash
npm run build
npm run preview
```

Run `npm run api` separately in production and configure your HTTPS reverse proxy
to serve the frontend and forward `/api` requests to the API. Set `NODE_ENV=production`
and `MYSQL_SSL=true` when using a MySQL server with a trusted TLS certificate.

## Accounts and saved study sets

Visitors can try the built-in demo sets without signing in. Creating and viewing
personal study sets requires an email and password account. Passwords are stored
as bcrypt hashes, and sessions use a random, HTTP-only, same-site cookie with a
seven-day expiry. The API rate-limits sign-in and registration requests.

Uploaded files are read in the browser and the extracted notes are sent by the
backend to Gemini for generation. The Gemini API key stays on the server. On
Google's free tier, submitted content may be used to improve its products, so
do not upload confidential notes. The full source text is not stored, but each
generated study set and its first 220 characters of source text are saved to
MySQL and private to its account. The API accepts up to 200,000 characters per
study set, keeps the 20 most recent sets per account, and rate-limits generation
requests. Larger inputs may take longer and use more tokens.
Account verification, password reset, and social sign-in are not included.

## What's inside

```
src/
  components/
    Navbar.tsx           — liquid-glass navbar, morphs on scroll
    BrainByteLogo.tsx     — original SVG logo, hover animation
    Hero.tsx               — full-screen hero, scroll-linked 3D + type reveal
    LiquidMetalScene.tsx   — R3F scene: distorted metallic blob, rings, glass panels
    WorldSection.tsx       — orbiting content fragments around a metallic sphere
    FeatureSection.tsx     — editorial 4-panel feature layout
    QuizPreview.tsx        — sample question UI used inside Quiz Arena
    TiltCard.tsx           — reusable cursor-tilt wrapper (disabled on touch)
    LiquidGlassPanel.tsx   — reusable frosted/liquid-glass surface
    HowItWorks.tsx         — 3-step cinematic scroll section
    FinalCTA.tsx           — closing section with an approaching metallic orb
    Footer.tsx
  hooks.ts                — usePrefersReducedMotion, useIsMobile
  App.tsx
  index.css               — palette, liquid-glass utility classes, grain overlay
```

## Notes

- Color palette is intentionally restricted to warm neutrals (ivory, cream, silver,
  bronze, graphite) — no blue/cyan anywhere, per the brief.
- `LiquidMetalScene` checks for WebGL support and falls back to a static gradient if
  it's unavailable, so the page never breaks.
- `prefers-reduced-motion` disables the shader's continuous motion and the entrance
  stagger; scroll-linked transforms remain (they're driven by user scrolling, not
  autoplaying animation).
- On small screens, `TiltCard`'s cursor tilt is disabled and the R3F scene renders at a
  lower device-pixel ratio; sections that used a 6-column CSS grid stack to one column.
- Fonts (Fraunces for display type, Inter for body) load from Google Fonts in
  `index.html` — swap for self-hosted files if you need to work offline.
