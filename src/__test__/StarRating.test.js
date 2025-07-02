import { render } from '@testing-library/react';
import StarRating from '../components/StarRating';

describe('StarRating', () => {
  it('renders without crashing', () => {
    render(<StarRating rating={4.5} />);
  });
});