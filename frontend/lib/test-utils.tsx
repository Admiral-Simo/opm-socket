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
  store?: any;
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

  function Wrapper({ children }: PropsWithChildren<{}>): JSX.Element {
    return <Provider store={store}>{children}</Provider>;
  }

  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}
