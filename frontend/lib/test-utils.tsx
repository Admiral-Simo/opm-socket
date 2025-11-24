import React, { PropsWithChildren } from "react";
import { render } from "@testing-library/react";
import type { RenderOptions } from "@testing-library/react";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { apiSlice } from "@/lib/services/api";
import chatReducer from "@/lib/services/chatSlice";
import type { RootState } from "@/lib/store";

export const setupTestStore = (preloadedState?: Partial<RootState>) => {
  return configureStore({
    reducer: {
      [apiSlice.reducerPath]: apiSlice.reducer,
      chat: chatReducer,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } as any,
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(apiSlice.middleware),
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    preloadedState: preloadedState as any,
  });
};

export type AppStore = ReturnType<typeof setupTestStore>;

interface ExtendedRenderOptions extends Omit<RenderOptions, "queries"> {
  preloadedState?: Partial<RootState>;
  store?: AppStore;
}

export function renderWithProviders(
  ui: React.ReactElement,
  {
    preloadedState = {},
    store = setupTestStore(preloadedState),
    ...renderOptions
  }: ExtendedRenderOptions = {},
) {
  function Wrapper({ children }: PropsWithChildren<unknown>): React.ReactNode {
    return <Provider store={store}>{children}</Provider>;
  }

  return {
    store,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ...render(ui, { wrapper: Wrapper as any, ...renderOptions }),
  };
}
