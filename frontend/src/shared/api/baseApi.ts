import { useState, useEffect } from 'react';

// Custom hook for polling the API
export function usePolling<T>(url: string, intervalMs: number = 5000) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    let isMounted = true;
    
    const fetchData = async () => {
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const json = await response.json();
        if (isMounted) {
          setData(json);
          setError(null);
        }
      } catch (e) {
        if (isMounted) {
          setError(e instanceof Error ? e : new Error('Unknown error'));
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    // Initial fetch
    fetchData();

    // Setup polling
    const intervalId = setInterval(fetchData, intervalMs);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [url, intervalMs]);

  return { data, error, isLoading };
}

// Action helper
export async function postAction(url: string, body?: unknown) {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  
  if (!response.ok) {
    throw new Error(`Failed to execute action: ${response.statusText}`);
  }
  
  return response.json();
}
