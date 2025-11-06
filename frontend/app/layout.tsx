import ReduxProvider from "../lib/ReduxProvider";
import AuthProvider from "./AuthProvider";
import "./globals.css";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body
        cz-shortcut-listen="true"
        data-new-gr-c-s-check-loaded="14.1261.0"
        data-gr-ext-installed=""
      >
        <AuthProvider>
          <ReduxProvider>{children}</ReduxProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
