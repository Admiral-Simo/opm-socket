import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import type { RootState } from "../store";

export interface PublicMessage {
  senderName: string;
  content: string;
  timestamp: string;
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
    addMessage: (state, action: PayloadAction<PublicMessage>) => {
      state.messages.push(action.payload);
    },
    setHistory: (state, action: PayloadAction<PublicMessage[]>) => {
      state.messages = action.payload;
    },
  },
});

export const { addMessage, setHistory } = chatSlice.actions;

export const selectPublicMessages = (state: RootState) => state.chat.messages;

export default chatSlice.reducer;
