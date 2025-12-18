import { render, fireEvent } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import Modal from '$lib/components/shared/Modal.svelte';

describe('Modal Component', () => {
  it('renders when isOpen is true', () => {
    const { getByRole } = render(Modal, {
      props: {
        isOpen: true,
        title: 'Test Modal'
      }
    });
    
    expect(getByRole('dialog')).toBeInTheDocument();
  });

  it('does not render when isOpen is false', () => {
    const { queryByRole } = render(Modal, {
      props: {
        isOpen: false,
        title: 'Test Modal'
      }
    });
    
    expect(queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('displays title correctly', () => {
    const { getByText } = render(Modal, {
      props: {
        isOpen: true,
        title: 'Custom Title'
      }
    });
    
    expect(getByText('Custom Title')).toBeInTheDocument();
  });

  it('closes when close button is clicked', async () => {
    let isOpen = true;
    const { getByLabelText } = render(Modal, {
      props: {
        isOpen,
        title: 'Test Modal',
        onClose: () => { isOpen = false; }
      }
    });
    
    await fireEvent.click(getByLabelText('Cerrar'));
    expect(isOpen).toBe(false);
  });

  it('closes when backdrop is clicked', async () => {
    let isOpen = true;
    const { container } = render(Modal, {
      props: {
        isOpen,
        title: 'Test Modal',
        onClose: () => { isOpen = false; }
      }
    });
    
    const backdrop = container.querySelector('.fixed.inset-0');
    await fireEvent.click(backdrop);
    expect(isOpen).toBe(false);
  });
});