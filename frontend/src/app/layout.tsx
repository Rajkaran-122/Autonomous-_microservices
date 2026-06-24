import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI SRE Platform | Autonomous Operations",
  description: "Enterprise-grade AI-powered Site Reliability Engineering dashboard.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        {children}
      </body>
    </html>
  );
}
