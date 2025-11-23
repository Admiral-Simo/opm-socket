"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import Link from "next/link";

export default function Header() {
  const { data: session, status } = useSession();

  return (
    <header className="flex w-full items-center justify-between bg-gray-800 px-8 py-4 shadow-lg">
      <Link
        href="/"
        className="text-2xl font-bold text-blue-400 hover:text-blue-300"
      >
        ChatApp
      </Link>

      <nav>
        {status === "loading" && (
          <div className="h-9 w-20 animate-pulse rounded bg-gray-600"></div>
        )}

        {status === "unauthenticated" && (
          <button
            onClick={() => signIn("keycloak")}
            className="rounded bg-blue-600 px-5 py-2 font-bold text-white transition-all hover:bg-blue-500"
          >
            Sign In
          </button>
        )}

        {status === "authenticated" && (
          <div className="flex items-center gap-4">
            <span className="text-gray-300">Hi, {session.user?.name}</span>
            <Link
              href="/friends"
              className="font-medium text-blue-400 hover:underline"
            >
              Friends
            </Link>
            <Link
              href="/chat"
              className="font-medium text-blue-400 hover:underline"
            >
              Public Chat
            </Link>

            <button
              onClick={() => signOut({ callbackUrl: "/" })}
              className="rounded bg-red-600 px-5 py-2 font-bold text-white transition-all hover:bg-red-500"
            >
              Sign Out
            </button>
          </div>
        )}
      </nav>
    </header>
  );
}
