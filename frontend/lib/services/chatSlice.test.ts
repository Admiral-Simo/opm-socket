import chatReducer, {
  addMessage,
  setHistory,
  PublicMessage,
} from "./chatSlice";

describe("chatSlice", () => {
  const initialState = {
    messages: [],
  };

  it("should handle initial state", () => {
    expect(chatReducer(undefined, { type: "unknown" })).toEqual({
      messages: [],
    });
  });

  it("should handle addMessage", () => {
    const newMessage: PublicMessage = {
      senderName: "Alice",
      content: "Hello World",
      timestamp: "2023-01-01T12:00:00Z",
    };

    const actual = chatReducer(initialState, addMessage(newMessage));
    expect(actual.messages).toHaveLength(1);
    expect(actual.messages[0]).toEqual(newMessage);
  });

  it("should handle setHistory", () => {
    const history: PublicMessage[] = [
      { senderName: "Bob", content: "Hi", timestamp: "2023-01-01" },
      { senderName: "Alice", content: "Hey", timestamp: "2023-01-02" },
    ];

    const actual = chatReducer(initialState, setHistory(history));
    expect(actual.messages).toHaveLength(2);
    expect(actual.messages).toEqual(history);
  });
});
