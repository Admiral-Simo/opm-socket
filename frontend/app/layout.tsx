import ReduxProvider from "../lib/ReduxProvider";
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
        <ReduxProvider>{children}</ReduxProvider>
      </body>
    </html>
  );
}
