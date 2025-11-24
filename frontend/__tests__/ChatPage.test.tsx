import { screen, fireEvent } from "@testing-library/react";
import { renderWithProviders } from "@/lib/test-utils";
import ChatPage from "@/app/(protected)/chat/page";
import * as wsProvider from "@/lib/WebSocketProvider";
import { useSession } from "next-auth/react";
import { useGetChatHistoryQuery } from "@/lib/services/api";

// --- Mocking API Hooks ---
jest.mock("@/lib/services/api", () => {
  const originalModule = jest.requireActual("@/lib/services/api");
  return {
    __esModule: true,
    ...originalModule,
    useGetChatHistoryQuery: jest.fn(),
    useSyncUserMutation: jest.fn(() => [jest.fn()]),
  };
});

jest.mock("@/lib/WebSocketProvider");

describe("ChatPage", () => {
  beforeEach(() => {
    (useSession as jest.Mock).mockReturnValue({
      data: { user: { name: "Test User" } },
      status: "authenticated",
    });

    // Default mock: empty history
    (useGetChatHistoryQuery as jest.Mock).mockReturnValue({
      data: [],
      isLoading: false,
      isSuccess: true,
      isError: false,
      refetch: jest.fn(),
    });
  });

  it("renders the chat room title", () => {
    jest.spyOn(wsProvider, "useWebSocket").mockReturnValue({
      stompClient: null,
      isConnected: false,
    });

    renderWithProviders(<ChatPage />);
    expect(screen.getByText(/Public Chat Room/i)).toBeInTheDocument();
  });

  it("shows messages from API data", () => {
    jest
      .spyOn(wsProvider, "useWebSocket")
      .mockReturnValue({ stompClient: null, isConnected: false });

    const mockMessages = [
      {
        senderName: "Alice",
        content: "Hello Test",
        timestamp: "2024-01-01T12:00:00",
      },
      {
        senderName: "Bob",
        content: "Reply Test",
        timestamp: "2024-01-02T12:00:00",
      },
    ];

    // --- FIX: Override the mock for THIS test only ---
    (useGetChatHistoryQuery as jest.Mock).mockReturnValue({
      data: mockMessages, // Return the data we want to see
      isLoading: false,
      isSuccess: true,
      isError: false,
      refetch: jest.fn(),
    });

    renderWithProviders(<ChatPage />);

    // Now the component will dispatch setHistory(mockMessages)
    // and the UI will update correctly.
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Hello Test")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
    expect(screen.getByText("Reply Test")).toBeInTheDocument();
  });

  it("sends a message via WebSocket when form is submitted", () => {
    const mockPublish = jest.fn();
    const mockSubscribe = jest.fn().mockReturnValue({ unsubscribe: jest.fn() });

    jest.spyOn(wsProvider, "useWebSocket").mockReturnValue({
      stompClient: {
        publish: mockPublish,
        subscribe: mockSubscribe,
      } as any,
      isConnected: true,
    });

    renderWithProviders(<ChatPage />);

    const input = screen.getByPlaceholderText(/Type your message/i);
    const button = screen.getByRole("button", { name: /Send/i });

    fireEvent.change(input, { target: { value: "New Message" } });
    fireEvent.click(button);

    expect(mockPublish).toHaveBeenCalledWith({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify({ content: "New Message" }),
    });

    expect((input as HTMLInputElement).value).toBe("");
  });
});
