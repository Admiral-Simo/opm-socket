import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { getSession } from "next-auth/react";
import { PublicMessage } from "./chatSlice";

const baseQuery = fetchBaseQuery({
  baseUrl: "http://localhost:8080/api",

  prepareHeaders: async (headers) => {
    const session = await getSession();

    const token = (session as any)?.accessToken;

    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    return headers;
  },
});

export const apiSlice = createApi({
  reducerPath: "api",
  baseQuery: baseQuery,
  endpoints: (builder) => ({
    getHello: builder.query<string, void>({
      query: () => "/test/hello",
    }),
    getChatHistory: builder.query<PublicMessage[], void>({
      query: () => "/chat/public/history",
    }),
    syncUser: builder.mutation<void, void>({
      query: () => ({ url: "/users/sync", method: "POST" }),
    }),
  }),
});

export const { useGetHelloQuery, useGetChatHistoryQuery, useSyncUserMutation } =
  apiSlice;
