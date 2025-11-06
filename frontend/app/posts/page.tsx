"use client";

import { useGetPostsQuery } from "../../lib/services/api";

export default function PostsPage() {
  const { data, error, isLoading } = useGetPostsQuery(null);

  if (isLoading) return <p>Loading...</p>;
  if (error) return <p>Error loading posts.</p>;

  return (
    <div className="container mx-auto">
      <h1 className="text-4xl text-bold my-10">Posts</h1>
      <ul>{data?.map((post: any) => <li key={post.id}>{post.title}</li>)}</ul>
    </div>
  );
}
