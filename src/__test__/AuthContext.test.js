jest.mock('axios');
import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';


describe('AuthContext', () => {
  test('provides authentication context', () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <div>Test Auth Context</div>
        </AuthProvider>
      </MemoryRouter>
    );
    expect(screen.getByText(/Test Auth Context/i)).toBeInTheDocument();
  });
});