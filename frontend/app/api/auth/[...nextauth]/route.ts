import NextAuth, { AuthOptions } from "next-auth";
import { JWT } from "next-auth/jwt";
import KeycloakProvider from "next-auth/providers/keycloak";

/**
 * This is the core logic for next-auth.
 * We are configuring it to use the Keycloak provider.
 *
 * We also add callbacks to ensure that the Keycloak `access_token`
 * is passed to the client-side session. This is VITAL for
 * calling your Spring Boot API later.
 */

export const authOptions: AuthOptions = {
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER!,
    }),
  ],

  callbacks: {
    /**
     * @param  {object}  token     Decrypted JSON Web Token
     * @param  {object}  account   Provider account (e.g., Keycloak)
     * @return {object}            Decrypted JSON Web Token
     */
    async jwt({ token, account }) {
      // On the initial sign-in, persist the access token to the JWT
      if (account) {
        token.accessToken = account.access_token;
      }
      return token;
    },

    /**
     * @param  {object}  session   Session object
     * @param  {object}  token     Decrypted JSON Web Token
     * @return {object}            Session object (what the client-side sees)
     */
    async session({ session, token }) {
      // Add the access token to the client-side session
      // We cast the session and token to `any` to avoid type errors
      // when adding custom properties.
      (session as any).accessToken = (token as any).accessToken;
      return session;
    },
  },
};

const handler = NextAuth(authOptions);

export { handler as GET, handler as POST };
