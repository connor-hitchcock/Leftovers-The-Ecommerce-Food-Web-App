import { is } from 'typescript-is';
import { castMock } from './utils';

jest.mock('typescript-is');

const mockedIs = castMock(is);

beforeEach(() => {
  jest.clearAllMocks();
});

it('Expect "is" mock to return mock value', () => {
  mockedIs.mockReturnValue(true);
  expect(is<number>('eleven')).toBeTruthy();
});

it('Expect "is" mock to be called with "seven"', () => {
  is<number>('seven');
  // Simply doing expect(mockedIs).toBeCalledWith('seven') does not work
  // There is an extra argument that seems to be provided (probably the actual "is" implementation function)
  expect(mockedIs.mock.calls[0][0]).toBe('seven');
});