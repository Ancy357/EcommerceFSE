jest.mock('axios');
import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import HomePage from '../components/HomePage';

describe('HomePage', () => {
  test('renders HomePage without crashing', () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    );
    expect(screen.getByText(/zylo elegance/i)).toBeInTheDocument();
  });
});

// This test file is temporarily disabled due to persistent test failures.
// Remove or fix the underlying issues before re-enabling.