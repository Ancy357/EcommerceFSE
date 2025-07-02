import { increaseQuantity } from '../services/cartService';
import { getProductById } from '../services/homePageService';
import { submitFeedback, fetchAllProducts } from '../services/orderService';

jest.mock('../api', () => ({
  get: jest.fn((url) => Promise.resolve({ data: { id: 1, name: 'Test Product', imageURL: 'test.jpg', availableStock: 10 } })),
  post: jest.fn((url, body) => Promise.resolve({ data: { success: true, feedback: body } })),
  put: jest.fn((url) => Promise.resolve({ data: { success: true } })),
  delete: jest.fn(() => Promise.resolve({ data: { success: true } })),
}));

describe('Service Functions', () => {
  it('increaseQuantity calls API and returns data', async () => {
    const res = await increaseQuantity('user1', 'prod1', 2);
    expect(res.data.success).toBe(true);
  });

  it('getProductById returns product data', async () => {
    const res = await getProductById(1);
    expect(res.data.name).toBe('Test Product');
  });

  it('submitFeedback posts feedback and returns success', async () => {
    const feedback = { rating: 5, comment: 'Great!' };
    const res = await submitFeedback(1, feedback);
    expect(res.data.success).toBe(true);
    expect(res.data.feedback).toEqual(feedback);
  });

  it('fetchAllProducts returns product list', async () => {
    const res = await fetchAllProducts();
    expect(res.data.name).toBe('Test Product');
  });
});
