/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        eco: {
          dark: '#0B132B',
          navy: '#1C2541',
          surface: '#1E293B',
          card: '#0F172A',
          green: '#10B981',
          lightgreen: '#34D399',
          amber: '#F59E0B',
          red: '#EF4444',
          cyan: '#06B6D4',
        },
      },
    },
  },
  plugins: [],
};
