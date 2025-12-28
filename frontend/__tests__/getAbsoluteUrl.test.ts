import { getAbsoluteUrl } from "@/lib/utils/url";

describe("getAbsoluteUrl", () => {
  afterEach(() => {
    delete process.env.NEXT_PUBLIC_API_URL;
  });

  it("prefixes /uploads paths with NEXT_PUBLIC_API_URL when set", () => {
    process.env.NEXT_PUBLIC_API_URL = "http://localhost:8080";

    expect(getAbsoluteUrl("/uploads/test.png")).toBe(
      "http://localhost:8080/uploads/test.png",
    );
  });

  it("returns absolute http URLs unchanged", () => {
    process.env.NEXT_PUBLIC_API_URL = "http://localhost:8080";

    expect(getAbsoluteUrl("http://example.com/file.jpg")).toBe(
      "http://example.com/file.jpg",
    );
  });

  it("returns relative /uploads when NEXT_PUBLIC_API_URL is not set", () => {
    delete process.env.NEXT_PUBLIC_API_URL;

    expect(getAbsoluteUrl("/uploads/test.png")).toBe("/uploads/test.png");
  });
});
