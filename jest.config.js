// jest.config.js
module.exports = {
    testEnvironment: 'jsdom',
    setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'], // Create this file
    transform: {
        '^.+\\.(js|jsx)$': 'babel-jest',
      },
      moduleFileExtensions: ['js', 'jsx'],
      transformIgnorePatterns: [
        "/node_modules/(?!(axios)/)"
    ],
    moduleNameMapper: {
        '\\.(css|less|scss|sass)$': 'identity-obj-proxy'
    }
  };