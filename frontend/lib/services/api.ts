import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { getSession } from "next-auth/react";

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

  responseHandler: (response) => response.text(),
});

export const apiSlice = createApi({
  reducerPath: "api",
  baseQuery: baseQuery,
  endpoints: (builder) => ({
    getHello: builder.query<string, void>({
      query: () => "/test/hello",
    }),
  }),
});

export const { useGetHelloQuery } = apiSlice;
