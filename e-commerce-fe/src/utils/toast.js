// Toast notification utility
let toastContainer = null;

const createToastContainer = () => {
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      pointer-events: none;
    `;
    document.body.appendChild(toastContainer);
  }
  return toastContainer;
};

const showToast = (message, type = 'error', duration = 5000) => {
  const container = createToastContainer();
  
  const toast = document.createElement('div');
  toast.style.cssText = `
    background: ${type === 'error' ? '#DC2626' : type === 'success' ? '#10B981' : '#3B82F6'};
    color: white;
    padding: 16px 24px;
    border-radius: 8px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    min-width: 300px;
    max-width: 500px;
    pointer-events: auto;
    animation: slideInRight 0.3s ease-out;
    display: flex;
    align-items: center;
    gap: 12px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  `;

  const icon = document.createElement('span');
  icon.style.cssText = 'font-size: 20px; flex-shrink: 0;';
  icon.textContent = type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️';

  const messageEl = document.createElement('div');
  messageEl.style.cssText = 'flex: 1; font-size: 14px; line-height: 1.5;';
  messageEl.textContent = message;

  const closeBtn = document.createElement('button');
  closeBtn.style.cssText = `
    background: transparent;
    border: none;
    color: white;
    cursor: pointer;
    font-size: 20px;
    padding: 0;
    margin-left: 8px;
    opacity: 0.8;
    transition: opacity 0.2s;
  `;
  closeBtn.textContent = '×';
  closeBtn.onmouseover = () => closeBtn.style.opacity = '1';
  closeBtn.onmouseout = () => closeBtn.style.opacity = '0.8';
  closeBtn.onclick = () => removeToast(toast);

  toast.appendChild(icon);
  toast.appendChild(messageEl);
  toast.appendChild(closeBtn);
  container.appendChild(toast);

  // Add CSS animation
  if (!document.getElementById('toast-animations')) {
    const style = document.createElement('style');
    style.id = 'toast-animations';
    style.textContent = `
      @keyframes slideInRight {
        from {
          transform: translateX(400px);
          opacity: 0;
        }
        to {
          transform: translateX(0);
          opacity: 1;
        }
      }
      @keyframes slideOutRight {
        from {
          transform: translateX(0);
          opacity: 1;
        }
        to {
          transform: translateX(400px);
          opacity: 0;
        }
      }
    `;
    document.head.appendChild(style);
  }

  // Auto remove after duration
  const timeoutId = setTimeout(() => removeToast(toast), duration);

  // Clear timeout on manual close
  toast.dataset.timeoutId = timeoutId;

  return toast;
};

const removeToast = (toast) => {
  if (toast.dataset.timeoutId) {
    clearTimeout(parseInt(toast.dataset.timeoutId));
  }
  
  toast.style.animation = 'slideOutRight 0.3s ease-out';
  setTimeout(() => {
    if (toast.parentNode) {
      toast.parentNode.removeChild(toast);
    }
    
    // Remove container if empty
    if (toastContainer && toastContainer.children.length === 0) {
      toastContainer.remove();
      toastContainer = null;
    }
  }, 300);
};

export const toast = {
  error: (message, duration) => showToast(message, 'error', duration),
  success: (message, duration) => showToast(message, 'success', duration),
  info: (message, duration) => showToast(message, 'info', duration),
};

export default toast;
