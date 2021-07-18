/**
 * Returns a promise to a point where all the previous messages in the JavaScript message queue have
 * been processed.
 *
 * @returns Empty Promise
 */
export function flushQueue() {
  // setTimeout pushes a message onto the end of the message queue. Therefore when the setTimeout
  // gets resolved then the message queue must have processed all the previous messages.
  return new Promise((resolve) => setTimeout(resolve, 0));
}

/**
 * Reinterprets the input argument as a jest mock.
 *
 * @param func Function that is mocked
 * @returns Input argument interpreted as a jest mock
 */
export function castMock<T, Y extends any[]>(func: (...args: Y) => T) {
  if (!jest.isMockFunction(func)) {
    throw new Error('Argument is not jest mock');
  }
  return <jest.Mock<T, Y>>func;
}
