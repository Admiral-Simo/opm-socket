export default function Home() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center py-2 bg-black">
      <h1 className="text-4xl font-bold text-white">
        Welcome to the Home Page{" "}
        <a
          className="py-1 px-2 rounded-md bg-white text-black underline"
          href="/posts"
        >
          Posts
        </a>
      </h1>
    </div>
  );
}
