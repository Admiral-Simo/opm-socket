export const getAbsoluteUrl = (url: string) => {
  const apiBase = process.env.NEXT_PUBLIC_API_URL || "";
  return url.startsWith("/uploads/") ? `${apiBase}${url}` : url;
};
