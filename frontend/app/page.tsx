"use client";

import { useSession, signIn, signOut } from "next-auth/react";

export default function Home() {
  const { data: session, status } = useSession();

  if (status === "loading") {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center p-24">
        <h1 className="text-2xl font-bold">Loading...</h1>
      </main>
    );
  }

  if (session) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center p-24">
        <h1 className="mb-4 text-4xl font-bold">
          Welcome, {session.user?.name}
        </h1>
        <p>this is your email {session.user?.email}</p>
        <p className="mb-6">You are signed in!</p>
        <pre className="mb-6 rounded container mx-auto p-4 text-left text-sm">
          <code>{JSON.stringify(session, null, 2)}</code>
        </pre>
        <a href="/chat">protected hello message</a>
        <button
          onClick={() => signOut()}
          className="rounded-lg bg-red-500 px-6 py-2 font-bold text-white transition-all hover:bg-red-600"
        >
          Sign Out
        </button>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1 className="mb-4 text-4xl font-bold">Chat App</h1>
      <p className="mb-6">You are not signed in.</p>
      <button
        onClick={() => signIn("keycloak")} // This must match your provider ID
        className="rounded-lg bg-blue-500 px-6 py-2 font-bold text-white transition-all hover:bg-blue-600"
      >
        Sign in with Keycloak
      </button>
    </main>
  );
}
