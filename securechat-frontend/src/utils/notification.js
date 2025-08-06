// A singleton container for notifications
let notificationContainer = null;

const getNotificationContainer = () => {
  if (!notificationContainer) {
    notificationContainer = document.createElement('div');
    notificationContainer.id = 'notification-container';
    notificationContainer.className = 'fixed top-4 right-4 z-[9999] w-full max-w-sm flex flex-col items-end space-y-2';
    document.body.appendChild(notificationContainer);
  }
  return notificationContainer;
};

export const showNotification = (title, message, type = 'info', duration = 3000) => {
  const container = getNotificationContainer();

  const notification = document.createElement('div');
  // Removed positioning classes (fixed, top-4, right-4) as the container now handles it.
  notification.className = `w-full p-4 rounded-2xl shadow-float transition-all duration-300 transform translate-x-full ${
    type === 'success' ? 'bg-green-500 text-white' :
    type === 'error' ? 'bg-red-500 text-white' :
    type === 'warning' ? 'bg-yellow-500 text-white' :
    'bg-blue-500 text-white'
  }`;
  
  if (document.documentElement.classList.contains('dark')) {
    notification.classList.add('dark');
  }

  notification.innerHTML = `
    <div class="flex items-center space-x-3">
      <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        ${type === 'success' ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>' :
          type === 'error' ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>' :
          type === 'warning' ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"></path>' :
          '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>'}
      </svg>
      <div>
        <div class="font-semibold">${title}</div>
        <div class="text-sm opacity-90 break-words">${message}</div>
      </div>
    </div>
  `;
  
  container.appendChild(notification);
  
  // Animate in
  setTimeout(() => {
    notification.classList.remove('translate-x-full');
  }, 100);
  
  // Remove after 'duration' seconds
  setTimeout(() => {
    notification.classList.add('opacity-0', 'translate-x-4'); // Smoother exit animation
    setTimeout(() => {
      container.removeChild(notification);
      // Optional: remove container if it's empty to clean up the DOM
      if (notificationContainer && container.children.length === 0) {
        document.body.removeChild(notificationContainer);
        notificationContainer = null;
      }
    }, 300); // Animation duration
  }, duration);
};

export const ElMessage = {
  success: (message) => showNotification('Success', message, 'success'),
  warning: (message) => showNotification('Warning', message, 'warning'),
  error: (message) => showNotification('Error', message, 'error'),
  info: (message) => showNotification('Info', message, 'info'),
};

export const ElNotification = {
  info: ({ title, message }) => showNotification(title, message, 'info', 4500),
  success: ({ title, message }) => showNotification(title, message, 'success', 4500),
  warning: ({ title, message }) => showNotification(title, message, 'warning', 4500),
  error: ({ title, message }) => showNotification(title, message, 'error', 4500),
};
