export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center pt-16 text-center">
      <h1 className="mb-4 text-5xl font-bold text-white">Welcome to ChatApp</h1>
      <p className="mb-8 text-lg text-gray-300">
        The world's most advanced chat platform.
      </p>
      <div className="rounded-lg bg-gray-800 p-6 shadow-lg">
        <h2 className="mb-2 text-2xl font-semibold text-white">Get Started</h2>
        <p className="text-gray-300">
          Please sign in using the button in the header to access your chats.
        </p>
      </div>
    </div>
  );
}
