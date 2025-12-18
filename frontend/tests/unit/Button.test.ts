import { render, fireEvent } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import Button from '$lib/components/shared/Button.svelte';

describe('Button Component', () => {
  it('renders with default props', () => {
    const { getByRole } = render(Button, { props: { children: 'Test Button' } });
    const button = getByRole('button');
    
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent('Test Button');
  });

  it('applies correct variant classes', () => {
    const { getByRole } = render(Button, { 
      props: { 
        variant: 'primary',
        children: 'Primary Button' 
      } 
    });
    const button = getByRole('button');
    
    expect(button).toHaveClass('bg-primary-600');
  });

  it('handles click events', async () => {
    const handleClick = vi.fn();
    const { getByRole } = render(Button, { 
      props: { 
        children: 'Click Me',
        onclick: handleClick
      } 
    });
    
    const button = getByRole('button');
    await fireEvent.click(button);
    
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('is disabled when loading', () => {
    const { getByRole } = render(Button, { 
      props: { 
        loading: true,
        children: 'Loading Button' 
      } 
    });
    const button = getByRole('button');
    
    expect(button).toBeDisabled();
  });

  it('shows loading state', () => {
    const { container } = render(Button, { 
      props: { 
        loading: true,
        children: 'Loading Button' 
      } 
    });
    
    expect(container.querySelector('.animate-spin')).toBeInTheDocument();
  });
});