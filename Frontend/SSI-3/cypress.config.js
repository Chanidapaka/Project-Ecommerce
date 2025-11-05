import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://intproj24.sit.kmutt.ac.th/ssi3/',
    baseAPI: 'http://intproj24.sit.kmutt.ac.th/ssi3/itb-mshop',
    experimentalRunAllSpecs: true,
    setupNodeEvents(on, config) {
    },
  },
});
