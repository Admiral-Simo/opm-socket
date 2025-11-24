import "@testing-library/jest-dom";

// 1. Polyfill Fetch for RTK Query
global.fetch = jest.fn(() =>
  Promise.resolve({
    json: () => Promise.resolve({}),
  }),
);

// 2. Mock Next.js Router
jest.mock("next/navigation", () => ({
  useRouter() {
    return {
      push: jest.fn(),
      replace: jest.fn(),
      prefetch: jest.fn(),
    };
  },
  usePathname() {
    return "";
  },
}));

// 3. Mock Next-Auth
jest.mock("next-auth/react", () => {
  const originalModule = jest.requireActual("next-auth/react");
  return {
    __esModule: true,
    ...originalModule,
    useSession: jest.fn(() => {
      return { data: null, status: "unauthenticated" };
    }),
    signIn: jest.fn(),
    signOut: jest.fn(),
  };
});
