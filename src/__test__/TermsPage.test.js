import { render } from '@testing-library/react';
import TermsPage from '../components/TermsPage';

describe('TermsPage', () => {
  it('renders without crashing', () => {
    render(<TermsPage />);
  });
});