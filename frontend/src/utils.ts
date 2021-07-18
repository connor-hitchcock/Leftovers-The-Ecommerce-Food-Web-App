/**
 * Sets a cookie. Will expire in one year.
 * @param name Name of the cookie being set.
 * @param value Value to store in the cookie.
 */
export function setCookie(name: string, value: string|number) {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  document.cookie = `${name}=${value}; expires=${date.toUTCString()}; path=/`;
}

/**
 * Reads a cookie.
 * @param name Name of the cookie to read.
 * @returns Value of the cookie. NULL if the cookie does not exist.
 */
export function getCookie(name: string) {
  let cookies = document.cookie.split(';');
  for (let cookie of cookies) {
    cookie = cookie.trim();
    if (cookie.startsWith(name + '=')) {
      return cookie;
    }
  }
  return null;
}

/**
 * Deletes a cookie.
 * @param name Name of the cookie to delete.
 */
export function deleteCookie(name: string) {
  document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;`;
}

/**
 * Cookie names.
 */
export const COOKIE = {
  USER: 'user'
};

/**
 * Converts a date to a human readable string
 *
 * @param date Date to format, either a date object or a parsable date string
 * @returns Date in a human readable string
 */
export function formatDate(date: Date | string) {
  if (typeof date === 'string') date = new Date(date);
  const parts = date.toDateString().split(' ');
  return `${parts[2]} ${parts[1]} ${parts[3]}`;
}

/**
 * User roles.
 */
export const USER_ROLES = {
  USER: 'user',
  GAA: 'globalApplicationAdmin',
  DGAA: 'defaultGlobalApplicationAdmin'
};




/**
 * Rate limits the input function by waiting a given amount of time calling the inner function.
 * If interrupted with another call then this wait is reset.
 *
 * @param func Function to rate limit
 * @param wait Time (ms) to wait before calling the function
 */
export function debounce(func: (() => void), wait: number) {
  let timeout: number | undefined;
  function debounced() {
    if (timeout !== undefined) {
      clearTimeout(timeout);
    }
    timeout = setTimeout(func, wait);
  }
  return debounced;
}

/**
 * Checks whether we are executing in the test enviroment
 *
 * @returns true if we're testing false otherwise
 */
export function isTesting() {
  return process.env.JEST_WORKER_ID !== undefined;
}