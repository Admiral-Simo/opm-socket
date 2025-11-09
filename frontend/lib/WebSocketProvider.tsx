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

interface WebSocketContextType {
  stompClient: Client | null;
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const [isConnected, setIsConnected] = useState(false);

  const clientRef = useRef<Client | null>(null);

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

      const stompClient = new Client({
        webSocketFactory: () => {
          return new SockJS("http://localhost:8080/ws");
        },

        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },

        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

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

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error("useWebSocket must be used within a WebSocketProvider");
  }
  return context;
};
