import { render, fireEvent } from '@testing-library/svelte';
import { describe, it, expect } from 'vitest';
import Input from '$lib/components/shared/Input.svelte';

describe('Input Component', () => {
  it('renders with label and placeholder', () => {
    const { getByLabelText, getByPlaceholderText } = render(Input, {
      props: {
        label: 'Test Label',
        placeholder: 'Test Placeholder',
        value: ''
      }
    });
    
    expect(getByLabelText('Test Label')).toBeInTheDocument();
    expect(getByPlaceholderText('Test Placeholder')).toBeInTheDocument();
  });

  it('shows error message when error prop is provided', () => {
    const { getByText } = render(Input, {
      props: {
        label: 'Test',
        value: '',
        error: 'This field is required'
      }
    });
    
    expect(getByText('This field is required')).toBeInTheDocument();
  });

  it('applies error styling when error exists', () => {
    const { getByRole } = render(Input, {
      props: {
        label: 'Test',
        value: '',
        error: 'Error message'
      }
    });
    
    const input = getByRole('textbox');
    expect(input).toHaveClass('border-red-500');
  });

  it('handles value changes', async () => {
    let value = '';
    const { getByRole } = render(Input, {
      props: {
        label: 'Test',
        value,
        onInput: (e) => { value = e.target.value; }
      }
    });
    
    const input = getByRole('textbox');
    await fireEvent.input(input, { target: { value: 'new value' } });
    
    expect(value).toBe('new value');
  });

  it('shows required indicator when required', () => {
    const { getByText } = render(Input, {
      props: {
        label: 'Required Field',
        value: '',
        required: true
      }
    });
    
    expect(getByText('*')).toBeInTheDocument();
  });
});