import { useState, useEffect } from 'react';
import App from './App';
import Workspace from './pages/Workspace';

export default function Router() {
  const [currentPath, setCurrentPath] = useState(window.location.hash.slice(1) || '/');

  useEffect(() => {
    const handleHashChange = () => {
      setCurrentPath(window.location.hash.slice(1) || '/');
    };

    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  // Make the navigation function available globally
  (window as any).navigateTo = (path: string) => {
    window.location.hash = path;
    setCurrentPath(path);
  };

  if (currentPath === '/workspace') {
    return <Workspace />;
  }

  return <App />;
}
