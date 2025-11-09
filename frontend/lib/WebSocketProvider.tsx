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

// 1. Define the shape of our context
interface WebSocketContextType {
  stompClient: Client | null;
  isConnected: boolean;
}

// 2. Create the context
const WebSocketContext = createContext<WebSocketContextType | null>(null);

// 3. Create the Provider component
export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const [isConnected, setIsConnected] = useState(false);

  // Use a ref to hold the client, as it's a long-lived object
  const clientRef = useRef<Client | null>(null);

  // Get the auth session
  const { data: session, status } = useSession();

  useEffect(() => {
    if (status === "loading") {
      return;
    }

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

    // C. If user is not authenticated, and we have a client, disconnect it
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

// 4. Create a custom hook to use the context
export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error("useWebSocket must be used within a WebSocketProvider");
  }
  return context;
};
