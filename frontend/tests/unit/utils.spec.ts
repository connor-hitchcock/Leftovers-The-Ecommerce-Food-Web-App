import { getCookie } from "@/utils";


describe('utils.ts', () => {
  const cookieString = 'test=7;test2=value;test3=that';
  const cookieStringWithSpaces = 'test=7; test2=value; test3=that';
  const cookieMapping: [string, string | null][] = [
    ['test', 'test=7'],
    ['test2', 'test2=value'],
    ['test3', 'test3=that'],
    ['nowhere', null],
  ];

  describe('cookie tests', () => {
    let originalDocument: Document;
    const cookieGetter = jest.fn();
    const cookieSetter = jest.fn();

    /**
     * Replaces the document with a mock version
     */
    beforeAll(() => {
      originalDocument = globalThis.document;
      globalThis.document = {} as Document;

      Object.defineProperty(globalThis.document, 'cookie', {
        get: cookieGetter,
        set: cookieSetter,
      });
    });

    /**
     * Restores the document
     */
    afterAll(() => {
      globalThis.document = originalDocument;
    });

    /**
     * Makes sure the mocks are clean
     */
    beforeEach(() => {
      jest.resetAllMocks();
    });

    it.each(cookieMapping)('Cookie with key "%s" should be parsed from a cookie string without spaces', (key: string, value: string | null) => {
      cookieGetter.mockReturnValue(cookieString);
      expect(getCookie(key)).toBe(value);
      expect(cookieGetter).toBeCalled();
      expect(cookieSetter).not.toBeCalled();
    });

    it.each(cookieMapping)('Cookie with key "%s" should be parsed from a cookie string without spaces', (key: string, value: string | null) => {
      cookieGetter.mockReturnValue(cookieStringWithSpaces);
      expect(getCookie(key)).toBe(value);
      expect(cookieGetter).toBeCalled();
      expect(cookieSetter).not.toBeCalled();
    });
  });
});