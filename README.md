# BrainByte

A cinematic, 3D quiz-platform landing page built with React 18, Vite, TypeScript, and Tailwind CSS. Three.js / React Three Fiber powers the liquid-metal scene, while Framer Motion handles scroll-driven animations.

## Run It

Install the dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the local URL displayed in your terminal, typically `http://localhost:5173`.

## Build for Production

```bash
npm run build
npm run preview
```

## What's Inside

```text
src/
├── components/
│   ├── Navbar.tsx
│   ├── BrainByteLogo.tsx
│   ├── Hero.tsx
│   ├── LiquidMetalScene.tsx
│   ├── WorldSection.tsx
│   ├── FeatureSection.tsx
│   ├── QuizPreview.tsx
│   ├── TiltCard.tsx
│   ├── LiquidGlassPanel.tsx
│   ├── HowItWorks.tsx
│   ├── FinalCTA.tsx
│   └── Footer.tsx
├── hooks.ts
├── App.tsx
└── index.css
```

## Features

* Cinematic 3D liquid-metal visuals.
* Warm, neutral color palette featuring ivory, cream, silver, bronze, and graphite.
* WebGL fallback for devices that cannot render the 3D scene.
* Reduced-motion support for accessibility.
* Responsive layouts for mobile devices.
* Fraunces and Inter typography loaded through Google Fonts.
