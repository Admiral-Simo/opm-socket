"use client";

import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useRef,
} from "react";
import { useSession } from "next-auth/react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

interface WebSocketContextType {
  stompClient: Client | null;
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const [isConnected, setIsConnected] = useState(false);
  const [stompClient, setStompClient] = useState<Client | null>(null);

  // We still use a ref to track the *active* client instance internally
  // to prevent cleanup effects from dealing with stale closures,
  // but we expose the 'stompClient' state to the context.
  const clientRef = useRef<Client | null>(null);

  const { data: session, status } = useSession();

  useEffect(() => {
    if (status === "loading") {
      return;
    }

    // CONNECT
    if (status === "authenticated" && session && !clientRef.current) {
      console.log("WebSocket: Session authenticated, attempting to connect...");

      // TypeScript now knows about accessToken thanks to next-auth.d.ts
      const token = session.accessToken;

      if (!token) {
        console.error("WebSocket: No access token found in session.");
        return;
      }

      const client = new Client({
        webSocketFactory: () => {
          return new SockJS("http://localhost:8080/ws");
        },
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log("WebSocket: Connected!");
          setIsConnected(true);
        },
        onDisconnect: () => {
          console.log("WebSocket: Disconnected.");
          setIsConnected(false);
        },
        onStompError: (frame) => {
          console.error(
            "WebSocket: Broker reported error: " + frame.headers["message"],
          );
          console.error("WebSocket: Error details: " + frame.body);
          setIsConnected(false);
        },
      });

      client.activate();
      clientRef.current = client;
      // eslint-disable-next-line
      setStompClient(client);
    }

    // DISCONNECT
    if (status === "unauthenticated" && clientRef.current) {
      console.log("WebSocket: Session lost, deactivating client...");
      clientRef.current.deactivate();
      clientRef.current = null;
      setStompClient(null);
      setIsConnected(false);
    }
  }, [status, session]);

  // We pass the state variable, not the ref, to the context
  const value = {
    stompClient,
    isConnected,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
}

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error("useWebSocket must be used within a WebSocketProvider");
  }
  return context;
};
