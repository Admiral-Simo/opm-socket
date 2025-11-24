import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { getSession } from "next-auth/react";
import { PublicMessage } from "./chatSlice";

export interface FriendDto {
  id: number;
  username: string;
  status: string;
}

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
  tagTypes: ["Friends"],
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

    getFriends: builder.query<FriendDto[], void>({
      query: () => "/friends",
      providesTags: ["Friends"],
    }),

    getFriendRequests: builder.query<FriendDto[], void>({
      query: () => "/friends/requests",
      providesTags: ["Friends"],
    }),

    sendFriendRequest: builder.mutation<void, string>({
      query: (username) => ({
        url: `/friends/request`,
        method: "POST",
        body: { targetUsername: username },
      }),
      invalidatesTags: ["Friends"],
    }),

    acceptFriendRequest: builder.mutation<void, number>({
      query: (friendshipId) => ({
        url: `/friends/accept/${friendshipId}`,
        method: "POST",
      }),
      invalidatesTags: ["Friends"],
    }),
  }),
});

export const {
  useGetHelloQuery,
  useGetChatHistoryQuery,
  useSyncUserMutation,
  useGetFriendsQuery,
  useGetFriendRequestsQuery,
  useSendFriendRequestMutation,
  useAcceptFriendRequestMutation,
} = apiSlice;
