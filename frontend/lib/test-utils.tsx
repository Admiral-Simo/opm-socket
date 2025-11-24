import React, { PropsWithChildren } from "react";
import { render } from "@testing-library/react";
import type { RenderOptions } from "@testing-library/react";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { apiSlice } from "@/lib/services/api";
import chatReducer from "@/lib/services/chatSlice";
import type { RootState } from "@/lib/store";

interface ExtendedRenderOptions extends Omit<RenderOptions, "queries"> {
  preloadedState?: Partial<RootState>;
  // We use 'unknown' or specific store type instead of any, but for test utils 'any' is often accepted practice
  // to allow flexible mocking. We'll use a generic approach here.
  store?: ReturnType<typeof configureStore>;
}

export function renderWithProviders(
  ui: React.ReactElement,
  { preloadedState = {}, store, ...renderOptions }: ExtendedRenderOptions = {},
) {
  if (!store) {
    store = configureStore({
      reducer: {
        [apiSlice.reducerPath || "api"]: apiSlice.reducer,
        chat: chatReducer,
      },
      middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(apiSlice.middleware),
      preloadedState,
    });
  }

  // PropsWithChildren is a generic, but standard usage implies children
  function Wrapper({ children }: PropsWithChildren<unknown>): JSX.Element {
    return <Provider store={store!}>{children}</Provider>;
  }

  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}
