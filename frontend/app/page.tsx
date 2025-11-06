import {
  ChatBubbleOvalLeftEllipsisIcon,
  PaperClipIcon,
  LockClosedIcon,
} from "@heroicons/react/24/outline";

export default function Home() {
  return (
    <div className="relative overflow-hidden">
      {/* Hero Section */}
      <section className="relative flex min-h-[calc(100vh-80px)] flex-col items-center justify-center py-20 text-center">
        {/* Abstract background glow */}
        <div className="absolute left-1/2 top-1/2 -z-10 h-96 w-96 -translate-x-1/2 -translate-y-1/2 rounded-full bg-blue-900/50 blur-[120px]" />

        <div className="container mx-auto max-w-4xl px-4">
          <h1 className="mb-6 bg-gradient-to-r from-blue-300 via-white to-blue-300 bg-clip-text text-5xl font-extrabold text-transparent md:text-7xl">
            Connect & Collaborate. Instantly.
          </h1>
          <p className="mb-12 text-lg text-gray-300 md:text-xl">
            Experience seamless real-time chat, secure file transfers, and
            enterprise-grade authentication.
            <br />
            All powered by a modern, high-performance tech stack.
          </p>
        </div>
      </section>

      {/* Features Section */}
      <section
        className="py-24"
        style={{ backgroundColor: "#111827" /* bg-gray-900 */ }}
      >
        <div className="container mx-auto max-w-6xl px-4">
          <h2 className="mb-16 text-center text-4xl font-bold text-white">
            Core Features
          </h2>
          <div className="grid grid-cols-1 gap-12 md:grid-cols-3">
            {/* Feature 1: Real-Time Chat */}
            <div className="flex flex-col items-center text-center">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-600 text-white">
                <ChatBubbleOvalLeftEllipsisIcon className="h-8 w-8" />
              </div>
              <h3 className="mb-3 text-2xl font-semibold text-white">
                Instant Messaging
              </h3>
              <p className="text-gray-400">
                Communicate in real-time with our WebSocket-powered chat. No
                delays, just instant connection.
              </p>
            </div>

            {/* Feature 2: Secure File Transfer */}
            <div className="flex flex-col items-center text-center">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-600 text-white">
                <PaperClipIcon className="h-8 w-8" />
              </div>
              <h3 className="mb-3 text-2xl font-semibold text-white">
                Secure File Sharing
              </h3>
              <p className="text-gray-400">
                Share documents, images, and project files with confidence. Your
                data is secured and transferred seamlessly.
              </p>
            </div>

            {/* Feature 3: Enterprise Security */}
            <div className="flex flex-col items-center text-center">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-600 text-white">
                <LockClosedIcon className="h-8 w-8" />
              </div>
              <h3 className="mb-3 text-2xl font-semibold text-white">
                Powered by Keycloak
              </h3>
              <p className="text-gray-400">
                State-of-the-art Identity and Access Management ensures your
                conversations and data are always protected.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Tech Stack Section */}
      <section
        className="py-24"
        style={{ backgroundColor: "#1F2937" /* bg-gray-800 */ }}
      >
        <div className="container mx-auto max-w-4xl px-4 text-center">
          <h2 className="mb-12 text-3xl font-bold text-white">
            Built with a Modern, Powerful Stack
          </h2>
          <div className="flex flex-wrap justify-center gap-4">
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              Spring Boot
            </span>
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              Next.js
            </span>
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              WebSockets (STOMP)
            </span>
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              Keycloak
            </span>
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              Redux Toolkit
            </span>
            <span className="rounded-full bg-gray-700 px-5 py-2 text-sm font-medium text-white">
              Tailwind CSS
            </span>
          </div>
        </div>
      </section>
    </div>
  );
}
