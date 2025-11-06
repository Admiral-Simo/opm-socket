import { Inter } from "next/font/google";
import "./globals.css"; // Your globals.css should have the base dark colors

import AuthProvider from "@/app/AuthProvider";
import ReduxProvider from "@/lib/ReduxProvider";
import Header from "@/components/Header";

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
          <ReduxProvider>
            <Header />
            <main className="p-8">{children}</main>
          </ReduxProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
