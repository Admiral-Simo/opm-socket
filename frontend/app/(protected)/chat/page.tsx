"use client";

import { useState, useEffect, FormEvent, useRef } from "react";
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

// Helper function to format the timestamp
function formatTimestamp(isoString: string) {
  try {
    return new Date(isoString).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (e) {
    return "just now";
  }
}

export default function ChatPage() {
  const dispatch = useAppDispatch();
  const messages = useAppSelector(selectPublicMessages); // <-- Pass the selector function directly

  const { stompClient, isConnected } = useWebSocket();
  const [currentMessage, setCurrentMessage] = useState("");
  const messageListRef = useRef<HTMLDivElement>(null);

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
    } catch (e) {
      console.error("Failed to send message:", e);
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
        {/*
          This line is now safe because 'messages' is guaranteed
          to be an array (initially [] from chatSlice).
        */}
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
                <p className="break-words rounded-lg bg-gray-700 px-3 py-2">
                  {msg.content}
                </p>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Message Input Form */}
      <form onSubmit={handleSendMessage} className="flex gap-2">
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
      </form>
    </div>
  );
}
