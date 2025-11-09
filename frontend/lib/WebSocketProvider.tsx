"use client";

import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useRef,
} from "react";
import { useSession } from "next-auth/react";
import { Client, type IFrame } from "@stomp/stompjs";
import SockJS from "sockjs-client";

// Define the shape of our context
interface WebSocketContextType {
  stompClient: Client | null;
  isConnected: boolean;
}

// Create the context
const WebSocketContext = createContext<WebSocketContextType | null>(null);

// Create the Provider component
export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const [isConnected, setIsConnected] = useState(false);

  // Use a ref to hold the client, as it's a long-lived object
  // and we don't want it to trigger re-renders on its own.
  const clientRef = useRef<Client | null>(null);

  // Get the auth session
  const { data: session, status } = useSession();

  useEffect(() => {
    // 1. Only run if the session is loaded
    if (status === "loading") {
      return;
    }

    // 2. If user is authenticated, and we don't have a client, create one
    if (status === "authenticated" && session && !clientRef.current) {
      console.log("WebSocket: Session authenticated, attempting to connect...");
      const token = (session as any).accessToken;

      if (!token) {
        console.error("WebSocket: No access token found in session.");
        return;
      }

      // Create the STOMP client
      const stompClient = new Client({
        // Use SockJS as the WebSocket factory
        webSocketFactory: () => {
          // This must match your Spring Boot port and endpoint
          return new SockJS("http://localhost:8080/ws");
        },

        // This is the CRITICAL part for security
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },

        // Heartbeat (optional, but good for stability)
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        // --- Event Handlers ---
        onConnect: (frame: IFrame) => {
          console.log("WebSocket: Connected!");
          setIsConnected(true);
          // You can subscribe to topics here if needed globally
        },
        onDisconnect: () => {
          console.log("WebSocket: Disconnected.");
          setIsConnected(false);
        },
        onStompError: (frame: IFrame) => {
          console.error(
            "WebSocket: Broker reported error: " + frame.headers["message"],
          );
          console.error("WebSocket: Error details: " + frame.body);
          setIsConnected(false);
        },
      });

      // Activate the client
      stompClient.activate();
      clientRef.current = stompClient;
    }

    // 3. If user is not authenticated, and we have a client, disconnect it
    if (status === "unauthenticated" && clientRef.current) {
      console.log("WebSocket: Session lost, deactivating client...");
      clientRef.current.deactivate();
      clientRef.current = null;
      setIsConnected(false);
    }
  }, [status, session]); // Re-run this effect when auth status changes

  const value = {
    stompClient: clientRef.current,
    isConnected,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
}

// Create a custom hook to use the context
export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error("useWebSocket must be used within a WebSocketProvider");
  }
  return context;
};
