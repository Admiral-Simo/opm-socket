"use client";
import {
  useAcceptFriendRequestMutation,
  useGetFriendRequestsQuery,
  useGetFriendsQuery,
  useSendFriendRequestMutation,
} from "@/lib/services/api";
import { useState } from "react";

function formatLastSeen(isoString: string) {
  if (!isoString) return "Offline";
  const lastSeenDate = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - lastSeenDate.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return "Last seen just now";
  if (diffMins < 60) return `Last seen ${diffMins}m ago`;
  if (diffHours < 24) return `Last seen ${diffHours}h ago`;
  return `Last seen ${diffDays}d ago`;
}

function FriendsPage() {
  const [targetUsername, setTargetUsername] = useState("");

  const { data: friends, isLoading: friendsLoading } = useGetFriendsQuery();
  const { data: requests, isLoading: requestsLoading } =
    useGetFriendRequestsQuery();
  const [sendRequest] = useSendFriendRequestMutation();
  const [acceptRequest] = useAcceptFriendRequestMutation();

  const handleAddFriend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetUsername) return;

    try {
      await sendRequest(targetUsername).unwrap();
      alert("Request sent!");
      setTargetUsername("");
    } catch {
      alert("No such username.");
    }
  };

  return (
    <div className="mx-auto max-w-4xl p-6 text-white">
      <h1 className="mb-8 text-4xl font-bold">Friends</h1>

      {/* --- SECTION 1: ADD FRIEND --- */}
      <div className="mb-10 rounded-lg bg-gray-800 p-6 shadow-lg">
        <h2 className="mb-4 text-xl font-semibold text-blue-400">Add Friend</h2>
        <form onSubmit={handleAddFriend} className="flex gap-4">
          <input
            type="text"
            placeholder="Enter username (e.g. simoo)"
            value={targetUsername}
            onChange={(e) => setTargetUsername(e.target.value)}
            className="flex-grow rounded bg-gray-700 p-2 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="rounded bg-blue-600 px-6 py-2 font-bold hover:bg-blue-500"
          >
            Send Request
          </button>
        </form>
      </div>

      <div className="grid gap-8 md:grid-cols-2">
        {/* --- SECTION 2: PENDING REQUESTS --- */}
        <div className="rounded-lg bg-gray-800 p-6 shadow-lg">
          <h2 className="mb-4 text-xl font-semibold text-yellow-400">
            Pending Requests ({requests?.length || 0})
          </h2>
          {requestsLoading ? (
            <p className="text-gray-400">Loading...</p>
          ) : requests?.length === 0 ? (
            <p className="text-gray-500">No pending requests.</p>
          ) : (
            <ul className="space-y-3">
              {requests?.map((req) => (
                <li
                  key={req.id}
                  className="flex items-center justify-between rounded bg-gray-700 p-3"
                >
                  <span className="font-medium">{req.username}</span>
                  <button
                    onClick={() => acceptRequest(req.id)}
                    className="rounded bg-green-600 px-3 py-1 text-sm font-bold hover:bg-green-500"
                  >
                    Accept
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* --- MY FRIENDS SECTION (UPDATED) --- */}
        <div className="rounded-lg bg-gray-800 p-6 shadow-lg">
          <h2 className="mb-4 text-xl font-semibold text-green-400">
            My Friends ({friends?.length || 0})
          </h2>
          {friendsLoading ? (
            <p className="text-gray-400">Loading...</p>
          ) : friends?.length === 0 ? (
            <p className="text-gray-500">No friends yet.</p>
          ) : (
            <ul className="space-y-3">
              {friends?.map((friend) => (
                <li
                  key={friend.id}
                  className="flex items-center justify-between rounded bg-gray-700 p-3"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{friend.username}</span>
                    {/* Last Seen Status (only if offline) */}
                    {!friend.online && (
                      <span className="text-xs text-gray-400">
                        {formatLastSeen(friend.lastSeen)}
                      </span>
                    )}
                  </div>

                  {/* Online/Offline Indicator */}
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-xs font-medium ${friend.online ? "text-green-400" : "text-gray-500"}`}
                    >
                      {friend.online ? "Online" : "Offline"}
                    </span>
                    <div
                      className={`h-3 w-3 rounded-full ${friend.online ? "bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.8)]" : "bg-gray-500"}`}
                    ></div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

export default FriendsPage;
