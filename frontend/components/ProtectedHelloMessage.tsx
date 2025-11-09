"use client";

import { useGetHelloQuery } from "@/lib/services/api";

export default function ProtectedHelloMessage() {
  const { data: helloMessage, isLoading, isError, error } = useGetHelloQuery();

  if (isLoading) {
    return (
      <div className="mt-4 rounded border border-blue-300 bg-blue-100 p-4">
        <p className="font-bold text-blue-800">
          Loading protected message from Spring Boot...
        </p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="mt-4 rounded border border-red-300 bg-red-100 p-4">
        <p className="font-bold text-red-800">Error loading message:</p>
        <pre className="text-sm text-black">
          {JSON.stringify(error, null, 2)}
        </pre>
      </div>
    );
  }

  return (
    <div className="mt-4 rounded border border-green-300 bg-green-100 p-4">
      <p className="font-bold text-green-800">
        Success! Message from Spring Boot:
      </p>
      <p className="text-gray-700">{helloMessage}</p>
    </div>
  );
}
