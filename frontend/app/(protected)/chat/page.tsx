"use client";

import ProtectedHelloMessage from "@/components/ProtectedHelloMessage"; // Adjust path
import { useSession, signOut } from "next-auth/react";

export default function ChatPage() {
  const { data: session } = useSession();

  return (
    <main className="flex min-h-screen flex-col items-center p-24">
      <h1 className="mb-4 text-4xl font-bold">
        Welcome to the Chat, {session?.user?.name}
      </h1>

      <button
        onClick={() => signOut()}
        className="rounded-lg bg-red-500 px-6 py-2 font-bold text-white transition-all hover:bg-red-600"
      >
        Sign Out
      </button>

      <ProtectedHelloMessage />
    </main>
  );
}
