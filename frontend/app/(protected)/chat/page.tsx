"use client";

import { useState, useEffect, FormEvent, useRef } from "react";
import { useSession } from "next-auth/react";
import { useWebSocket } from "@/lib/WebSocketProvider";
import { useAppDispatch, useAppSelector } from "@/lib/hooks";
import {
  addMessage,
  selectPublicMessages,
  PublicMessage,
} from "@/lib/services/chatSlice";
import { type IMessage } from "@stomp/stompjs";

import ProtectedHelloMessage from "@/components/ProtectedHelloMessage";

export default function ChatPage() {
  const { data: session } = useSession();

  const dispatch = useAppDispatch();
  const messages = useAppSelector(selectPublicMessages);

  const { stompClient, isConnected } = useWebSocket();

  const [currentMessage, setCurrentMessage] = useState("");

  const messageListRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isConnected && stompClient) {
      console.log("ChatPage: Subscribing to /topic/public");

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

      // Unsubscribe on component unmount
      return () => {
        console.log("ChatPage: Unsubscribing from /topic/public");
        subscription.unsubscribe();
      };
    }
  }, [isConnected, stompClient, dispatch]);

  useEffect(() => {
    if (messageListRef.current) {
      messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSendMessage = (e: FormEvent) => {
    e.preventDefault();
    if (currentMessage.trim() === "" || !isConnected || !stompClient) {
      return;
    }

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
      <p className="mb-4 text-gray-400">
        You are connected as: {session?.user?.name}
      </p>

      {/* Message List */}
      <div
        className="mb-4 h-96 overflow-y-auto rounded-lg bg-gray-800 p-4"
        ref={messageListRef}
      >
        {messages.length === 0 && (
          <div className="flex h-full items-center justify-center">
            <p className="text-gray-500">No messages yet. Say hello!</p>
          </div>
        )}
        <ul className="space-y-3">
          {messages.map((msg, index) => (
            <li key={index} className="flex flex-col">
              <span className="text-sm font-bold text-blue-300">
                {msg.senderName}
              </span>
              <p className="break-words rounded-lg bg-gray-700 px-3 py-2">
                {msg.content}
              </p>
            </li>
          ))}
        </ul>
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

      <div className="mt-8">
        <ProtectedHelloMessage />
      </div>
    </div>
  );
}
