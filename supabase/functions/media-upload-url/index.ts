import { createClient } from "npm:@supabase/supabase-js@2";
import { createRemoteJWKSet, jwtVerify } from "npm:jose@5";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const FIREBASE_PROJECT_ID = Deno.env.get("FIREBASE_PROJECT_ID") ?? "";
const BUCKET = "media";

const ALLOWED_PREFIXES = [
  "listening/images/",
  "listening/audio/",
  "vocabulary/images/",
  "vocabulary/audio/",
  "avatars/",
];

const ALLOWED_CONTENT_TYPES = new Set([
  "image/jpeg",
  "image/png",
  "image/webp",
  "audio/mpeg",
  "audio/mp4",
  "audio/aac",
  "audio/wav",
  "audio/x-wav",
  "audio/ogg",
]);

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-firebase-id-token",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const JWKS = createRemoteJWKSet(
  new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"),
);

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
  });
}

async function verifyFirebaseToken(token: string): Promise<string> {
  if (!FIREBASE_PROJECT_ID) {
    throw new Error("FIREBASE_PROJECT_ID is not configured");
  }
  const { payload } = await jwtVerify(token, JWKS, {
    issuer: `https://securetoken.google.com/${FIREBASE_PROJECT_ID}`,
    audience: FIREBASE_PROJECT_ID,
  });
  if (!payload.sub) {
    throw new Error("Token has no subject");
  }
  return payload.sub;
}

async function isAdmin(uid: string, idToken: string): Promise<boolean> {
  if (!FIREBASE_PROJECT_ID) {
    return false;
  }
  const url = `https://firestore.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}`
    + `/databases/(default)/documents/admins/${encodeURIComponent(uid)}`;
  const response = await fetch(url, {
    headers: { Authorization: `Bearer ${idToken}` },
  });
  return response.ok;
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: CORS_HEADERS });
  }
  if (req.method !== "POST") {
    return json({ error: "Method not allowed" }, 405);
  }
  if (!SUPABASE_URL || !SERVICE_ROLE_KEY) {
    return json({ error: "Supabase environment is not configured" }, 500);
  }

  try {
    const idToken = req.headers.get("x-firebase-id-token") ?? "";
    if (!idToken) {
      return json({ error: "Missing Firebase ID token" }, 401);
    }

    const uid = await verifyFirebaseToken(idToken);

    const body = await req.json().catch(() => ({}));
    const path = String(body.path ?? "");
    const contentType = String(body.contentType ?? "");

    if (!path || path.includes("..") || path.startsWith("/")) {
      return json({ error: "Invalid upload path" }, 400);
    }
    if (!ALLOWED_PREFIXES.some((prefix) => path.startsWith(prefix))) {
      return json({ error: "Upload path is not allowed" }, 400);
    }
    if (!ALLOWED_CONTENT_TYPES.has(contentType)) {
      return json({ error: "Unsupported content type" }, 400);
    }

    const isAvatarUpload = path.startsWith(`avatars/${uid}/`);
    if (!isAvatarUpload && !(await isAdmin(uid, idToken))) {
      return json({ error: "Admin access required" }, 403);
    }

    const supabase = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, {
      auth: { persistSession: false, autoRefreshToken: false },
    });

    const { data, error } = await supabase.storage
      .from(BUCKET)
      .createSignedUploadUrl(path, { upsert: true });

    if (error || !data) {
      throw error ?? new Error("Could not create upload URL");
    }

    const publicUrl = supabase.storage.from(BUCKET).getPublicUrl(path).data.publicUrl;

    return json({
      signedUrl: data.signedUrl,
      token: data.token,
      path: data.path ?? path,
      publicUrl,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unexpected error";
    return json({ error: message }, 401);
  }
});
