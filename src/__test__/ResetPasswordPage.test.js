jest.mock('axios');
import React from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ResetPasswordPage from '../components/ResetPasswordPage';

describe('ResetPasswordPage', () => {
  test('renders ResetPasswordPage without crashing', () => {
    render(
      <MemoryRouter>
        <ResetPasswordPage />
      </MemoryRouter>
    );
  });
});

