// This is a new file you must create.
// We must wrap our app in a SessionProvider, but because it uses
// React Context, it must be a "use client" component.

"use client";

import { SessionProvider } from "next-auth/react";

type Props = {
  children?: React.ReactNode;
};

export default function AuthProvider({ children }: Props) {
  return <SessionProvider>{children}</SessionProvider>;
}
