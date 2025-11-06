"use client";

import { useGetHelloQuery } from "@/lib/services/api";

export default function ProtectedHelloMessage() {
  const { data: helloMessage, isLoading, isError, error } = useGetHelloQuery();

  if (isLoading) {
    return (
      <div className="mt-4 rounded border border-blue-700 bg-blue-900 p-4">
        <p className="font-bold text-blue-200">
          Loading protected message from Spring Boot...
        </p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="mt-4 rounded border border-red-700 bg-red-900 p-4">
        <p className="font-bold text-red-200">Error loading message:</p>
        <pre className="text-sm text-red-100">
          {JSON.stringify(error, null, 2)}
        </pre>
      </div>
    );
  }

  return (
    <div className="mt-4 rounded border border-green-700 bg-green-900 p-4">
      <p className="font-bold text-green-200">
        Success! Message from Spring Boot:
      </p>
      <p className="text-gray-300">{helloMessage}</p>
    </div>
  );
}
