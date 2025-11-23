"use client";

import { useSyncUserMutation } from "@/lib/services/api";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect, useRef } from "react";

export default function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { status } = useSession();
  const router = useRouter();

  const [syncUser] = useSyncUserMutation();

  const hasSynced = useRef(false);

  useEffect(() => {
    if (status === "loading") {
      return;
    }

    if (status === "unauthenticated") {
      router.push("/");
    }

    if (status === "authenticated" && !hasSynced.current) {
      syncUser()
        .unwrap()
        .then(() => {
          console.log("User synchronized successfully.");
        })
        .catch((err: Error) => {
          console.error("Error synchronizing user:", err);
        });

      hasSynced.current = true;
    }
  }, [status, router, syncUser]);

  if (status === "loading") {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p>Loading session...</p>
      </div>
    );
  }

  if (status === "authenticated") {
    return <>{children}</>;
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <p>Redirecting to login...</p>
    </div>
  );
}
