/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0fdf4',
          100: '#dcfce7',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
        },
        care: {
          blue: '#1e40af',
          sky: '#0284c7',
          rose: '#e11d48',
          amber: '#d97706',
        }
      }
    },
  },
  plugins: [],
}
