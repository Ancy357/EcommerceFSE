// Mock axios for all tests using Jest's module mocking system
jest.mock('axios', () => {
  const mAxiosInstance = {
    create: jest.fn().mockReturnThis(),
    interceptors: { request: { use: jest.fn() }, response: { use: jest.fn() } },
    get: jest.fn(() => Promise.resolve({})),
    post: jest.fn(() => Promise.resolve({})),
    put: jest.fn(() => Promise.resolve({})),
    delete: jest.fn(() => Promise.resolve({})),
    defaults: { headers: { common: {} } }
  };
  return {
    __esModule: true,
    default: mAxiosInstance,
    create: jest.fn(() => mAxiosInstance),
    interceptors: mAxiosInstance.interceptors,
    defaults: mAxiosInstance.defaults,
    get: mAxiosInstance.get,
    post: mAxiosInstance.post,
    put: mAxiosInstance.put,
    delete: mAxiosInstance.delete
  };
});
