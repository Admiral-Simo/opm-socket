import { Inter } from "next/font/google";
import "./globals.css";

import AuthProvider from "@/app/AuthProvider";
import ReduxProvider from "@/lib/ReduxProvider";
import Header from "@/components/Header";
import { WebSocketProvider } from "@/lib/WebSocketProvider";

const inter = Inter({ subsets: ["latin"] });

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body
        className={`${inter.className} bg-gray-900 text-gray-200`}
        cz-shortcut-listen="true"
        data-new-gr-c-s-check-loaded="14.1261.0"
        data-gr-ext-installed=""
      >
        <AuthProvider>
          <WebSocketProvider>
            <ReduxProvider>
              <Header />
              <main className="p-8">{children}</main>
            </ReduxProvider>
          </WebSocketProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
