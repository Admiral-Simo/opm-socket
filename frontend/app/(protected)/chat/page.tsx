"use client";

import { useState, useEffect, FormEvent, useRef } from "react";
import { useSession } from "next-auth/react";
import { useWebSocket } from "@/lib/WebSocketProvider";
import { useAppDispatch, useAppSelector } from "@/lib/hooks";
import {
  addMessage,
  setHistory,
  selectPublicMessages, // 2. Import the selector function
  PublicMessage,
} from "@/lib/services/chatSlice"; // Adjust path as needed
import { useGetChatHistoryQuery } from "@/lib/services/api"; // 3. Import the RTK Query hook
import { type IMessage } from "@stomp/stompjs";
import { getAbsoluteUrl } from "@/lib/utils/url";
import Modal from "@/components/Modal";

// Helper function to format the timestamp
function formatTimestamp(isoString: string) {
  try {
    return new Date(isoString).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return "just now";
  }
}

function isImageUrl(url: string) {
  return url.match(/\.(png|jpe?g|gif|webp|avif)(\?|$)/i);
}

export default function ChatPage() {
  const dispatch = useAppDispatch();
  const messages = useAppSelector(selectPublicMessages); // <-- Pass the selector function directly

  const { stompClient, isConnected } = useWebSocket();
  const [currentMessage, setCurrentMessage] = useState("");
  const messageListRef = useRef<HTMLDivElement>(null);

  const { data: session } = useSession();

  const {
    data: history,
    isLoading: isHistoryLoading,
    isSuccess: isHistorySuccess,
  } = useGetChatHistoryQuery();

  // Load history into Redux when it arrives
  useEffect(() => {
    if (isHistorySuccess && history) {
      dispatch(setHistory(history));
    }
  }, [isHistorySuccess, history, dispatch]);

  // Subscribe to WebSocket *after* history is loaded
  useEffect(() => {
    if (isConnected && stompClient && isHistorySuccess) {
      console.log("ChatPage: History loaded, subscribing to /topic/public");

      const subscription = stompClient.subscribe(
        "/topic/public",
        (message: IMessage) => {
          try {
            const publicMessage: PublicMessage = JSON.parse(message.body);
            dispatch(addMessage(publicMessage));
          } catch (e) {
            console.error("Failed to parse incoming message:", message.body, e);
          }
        },
      );

      return () => {
        console.log("ChatPage: Unsubscribing from /topic/public");
        subscription.unsubscribe();
      };
    }
  }, [isConnected, stompClient, dispatch, isHistorySuccess]);

  // Scroll to bottom when new messages are added
  useEffect(() => {
    if (messageListRef.current) {
      messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSendMessage = (e: FormEvent) => {
    e.preventDefault();
    if (currentMessage.trim() === "" || !isConnected || !stompClient) return;
    try {
      stompClient.publish({
        destination: "/app/chat.sendMessage",
        body: JSON.stringify({ content: currentMessage }),
      });
      setCurrentMessage("");
    } catch (error) {
      console.error("Failed to send message:", error);
    }
  };

  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const uploadFile = async (file: File | null) => {
    if (!file) return;
    if (!session?.accessToken) {
      console.error("No access token for upload");
      setUploadError("Not authenticated");
      // auto-clear
      setTimeout(() => setUploadError(null), 5000);
      return;
    }

    const form = new FormData();
    form.append("file", file);
    setIsUploading(true);
    setUploadError(null);

    try {
      const res = await fetch("http://localhost:8080/upload", {
        method: "POST",
        body: form,
        headers: {
          Authorization: `Bearer ${session.accessToken}`,
        },
      });

      if (!res.ok) {
        const error = await res.json().catch(() => ({}));
        console.error("Upload failed:", error);
        setUploadError(error?.error || "Upload failed");
        // auto-clear message
        setTimeout(() => setUploadError(null), 5000);
        return;
      }

      const data = await res.json();
      const url = data.url as string;

      // send the URL as a chat message via WebSocket
      if (isConnected && stompClient) {
        stompClient.publish({
          destination: "/app/chat.sendMessage",
          body: JSON.stringify({ content: url }),
        });
      }
    } catch (err) {
      console.error("Upload error:", err);
      setUploadError("Network or server error during upload");
      setTimeout(() => setUploadError(null), 5000);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="mx-auto max-w-4xl">
      <h1 className="mb-4 text-4xl font-bold">Public Chat Room</h1>

      {/* Message List */}
      <div
        className="mb-4 h-96 overflow-y-auto rounded-lg bg-gray-800 p-4"
        ref={messageListRef}
      >
        {isHistoryLoading && (
          <div className="flex h-full items-center justify-center">
            <p className="text-gray-500">Loading history...</p>
          </div>
        )}
        {isHistorySuccess && messages.length === 0 && (
          <div className="flex h-full items-center justify-center">
            <p className="text-gray-500">No messages yet. Say hello!</p>
          </div>
        )}

        {isHistorySuccess && (
          <ul className="space-y-3">
            {messages.map((msg, index) => (
              <li key={index} className="flex flex-col">
                <div className="flex items-baseline gap-2">
                  <span className="text-sm font-bold text-blue-300">
                    {msg.senderName}
                  </span>
                  <span className="text-xs text-gray-500">
                    {formatTimestamp(msg.timestamp)}
                  </span>
                </div>

                <div className="break-words rounded-lg bg-gray-700 px-3 py-2">
                  {msg.content.startsWith("/uploads/") || msg.content.startsWith("http") ? (
                    isImageUrl(msg.content) ? (
                      <img src={getAbsoluteUrl(msg.content)} alt="Uploaded file" className="max-w-full" />
                    ) : (
                      <a href={getAbsoluteUrl(msg.content)} target="_blank" rel="noreferrer" className="text-blue-300 underline">
                        {msg.content}
                      </a>
                    )
                  ) : (
                    <p>{msg.content}</p>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Message Input Form */}
      <div className="flex gap-2">
        <form onSubmit={handleSendMessage} className="flex-grow">
          <div className="flex gap-2">
            <input
              type="text"
              value={currentMessage}
              onChange={(e) => setCurrentMessage(e.target.value)}
              placeholder={isConnected ? "Type your message..." : "Connecting..."}
              disabled={!isConnected}
              className="flex-grow rounded-lg border-gray-600 bg-gray-700 p-3 text-white placeholder-gray-500 focus:border-blue-500 focus:ring-blue-500"
            />
            <button
              type="submit"
              disabled={!isConnected || currentMessage.trim() === ""}
              className="rounded-lg bg-blue-600 px-6 py-3 font-bold text-white transition-all hover:bg-blue-500 disabled:opacity-50"
            >
              Send
            </button>
          </div>
        </form>

        {/* File upload control */}
        <div className="flex items-center gap-2">
          <label className="cursor-pointer rounded-lg bg-gray-700 px-4 py-3 text-white">
            📎
            <input
              type="file"
              className="hidden"
              onChange={(e) => uploadFile(e.target.files ? e.target.files[0] : null)}
            />
          </label>
          <div className="flex flex-col">
            {isUploading && <span className="text-sm text-gray-300">Uploading...</span>}
            {/* Display upload errors in a modal instead of inline text */}
          <Modal open={!!uploadError} title="Upload Error" message={uploadError ?? ""} onClose={() => setUploadError(null)} />
          </div>
        </div>
      </div>
    </div>
  );
}
