import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import type { RootState } from "../store";

export interface PublicMessage {
  senderName: string;
  content: string;
}

interface ChatState {
  messages: PublicMessage[];
}

const initialState: ChatState = {
  messages: [],
};

export const chatSlice = createSlice({
  name: "chat",
  initialState,
  reducers: {
    // This reducer adds a new message to the list
    addMessage: (state, action: PayloadAction<PublicMessage>) => {
      state.messages.push(action.payload);
      // Optional: you could also add logic here to limit
      // the number of messages stored (e.g., state.messages.slice(-100))
    },
    // You could add a 'clearMessages' reducer here if needed
  },
});

// Export the reducer and the action
export const { addMessage } = chatSlice.actions;

// Create a selector to get the messages from the state
export const selectPublicMessages = (state: RootState) => state.chat.messages;

export default chatSlice.reducer;
