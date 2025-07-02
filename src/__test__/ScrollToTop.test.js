import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ScrollToTop from '../components/ScrollToTop';

describe('ScrollToTop', () => {
  it('renders without crashing', () => {
    render(
      <MemoryRouter>
        <ScrollToTop />
      </MemoryRouter>
    );
  });
});