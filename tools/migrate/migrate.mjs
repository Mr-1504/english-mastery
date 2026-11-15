import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import admin from "firebase-admin";
import { createClient } from "@supabase/supabase-js";
import lamejs from "@breezystack/lamejs";
import initSqlJs from "sql.js";
import tus from "tus-js-client";
import ws from "ws";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(HERE, "../..");

const DB_PATH = path.resolve(process.env.DB_PATH ?? path.join(
  REPO_ROOT,
  "PracticeToeic/practicetoeicapp/app/src/main/assets/databases/HocNgonNgu.db",
));
const NGHE_DIR = path.resolve(process.env.NGHE_DIR ?? path.join(REPO_ROOT, "nghe"));
const TUVUNG_DIR = path.resolve(process.env.TUVUNG_DIR ?? path.join(REPO_ROOT, "tuvung"));
const SUPABASE_URL = process.env.SUPABASE_URL ?? "";
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY ?? "";
const BUCKET = process.env.SUPABASE_MEDIA_BUCKET ?? "media";
const AUDIO_BITRATE = Number(process.env.AUDIO_BITRATE ?? 64);
const INCLUDE_PLACEHOLDER_VOCAB = process.env.INCLUDE_PLACEHOLDER_VOCAB === "true";
const DRY_RUN = process.env.DRY_RUN === "true";

if (!fs.existsSync(DB_PATH)) {
  throw new Error(`SQLite database not found: ${DB_PATH}`);
}
if (!DRY_RUN && (!SUPABASE_URL || !SUPABASE_SERVICE_ROLE_KEY)) {
  throw new Error("SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY are required");
}

const supabase = DRY_RUN
  ? null
  : createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
      auth: { persistSession: false, autoRefreshToken: false },
      realtime: { transport: ws },
    });

let firestore = null;
if (!DRY_RUN) {
  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
    });
  }
  firestore = admin.firestore();
}

const SQL = await initSqlJs();
const sqlite = new SQL.Database(fs.readFileSync(DB_PATH));

function rows(table) {
  const result = sqlite.exec(`SELECT * FROM ${table}`);
  if (!result.length) {
    return [];
  }
  const { columns, values } = result[0];
  return values.map((value) => Object.fromEntries(columns.map((column, i) => [column, value[i]])));
}

function asText(value) {
  if (value == null) {
    return "";
  }
  if (value instanceof Uint8Array) {
    return Buffer.from(value).toString("utf8");
  }
  return String(value);
}

function asNumber(value) {
  return Number(value ?? 0);
}

function imageInfo(bytes) {
  if (!bytes || bytes.length < 12) {
    return { ext: "jpg", contentType: "image/jpeg" };
  }
  if (bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) {
    return { ext: "png", contentType: "image/png" };
  }
  if (
    bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x46
    && bytes[8] === 0x57 && bytes[9] === 0x45 && bytes[10] === 0x42 && bytes[11] === 0x50
  ) {
    return { ext: "webp", contentType: "image/webp" };
  }
  return { ext: "jpg", contentType: "image/jpeg" };
}

function wavToMp3(wavBytes, bitrate = AUDIO_BITRATE) {
  let offset = 12;
  let fmt = null;
  let data = null;

  while (offset + 8 <= wavBytes.length) {
    const chunkId = wavBytes.toString("ascii", offset, offset + 4);
    const chunkSize = wavBytes.readUInt32LE(offset + 4);
    if (chunkId === "fmt ") {
      fmt = {
        audioFormat: wavBytes.readUInt16LE(offset + 8),
        channels: wavBytes.readUInt16LE(offset + 10),
        sampleRate: wavBytes.readUInt32LE(offset + 12),
        bitsPerSample: wavBytes.readUInt16LE(offset + 22),
      };
    } else if (chunkId === "data") {
      data = wavBytes.subarray(offset + 8, offset + 8 + chunkSize);
      break;
    }
    offset += 8 + chunkSize + (chunkSize % 2);
  }

  if (!fmt || !data) {
    throw new Error("WAV file is missing fmt/data chunks");
  }
  if (fmt.audioFormat !== 1 || fmt.bitsPerSample !== 16) {
    throw new Error("Only 16-bit PCM WAV files are supported");
  }

  const frameCount = Math.floor(data.length / (fmt.channels * 2));
  const mono = new Int16Array(frameCount);
  for (let i = 0; i < frameCount; i++) {
    let sum = 0;
    for (let channel = 0; channel < fmt.channels; channel++) {
      sum += data.readInt16LE((i * fmt.channels + channel) * 2);
    }
    mono[i] = Math.max(-32768, Math.min(32767, Math.round(sum / fmt.channels)));
  }

  const encoder = new lamejs.Mp3Encoder(1, fmt.sampleRate, bitrate);
  const chunks = [];
  const blockSize = 1152;
  for (let i = 0; i < mono.length; i += blockSize) {
    const chunk = encoder.encodeBuffer(mono.subarray(i, i + blockSize));
    if (chunk.length) {
      chunks.push(Buffer.from(chunk));
    }
  }
  const tail = encoder.flush();
  if (tail.length) {
    chunks.push(Buffer.from(tail));
  }
  return Buffer.concat(chunks);
}

function uploadResumable(objectPath, bytes, contentType) {
  return new Promise((resolve, reject) => {
    let lastLoggedPercent = -1;
    const upload = new tus.Upload(bytes, {
      endpoint: `${SUPABASE_URL}/storage/v1/upload/resumable`,
      retryDelays: [0, 1000, 3000, 5000, 10000, 20000, 30000, 60000],
      headers: {
        authorization: `Bearer ${SUPABASE_SERVICE_ROLE_KEY}`,
        "x-upsert": "true",
      },
      uploadDataDuringCreation: true,
      removeFingerprintOnSuccess: true,
      metadata: {
        bucketName: BUCKET,
        objectName: objectPath,
        contentType,
        cacheControl: "3600",
      },
      chunkSize: 256 * 1024,
      onProgress(uploaded, total) {
        if (total < 1024 * 1024) {
          return;
        }
        const percent = Math.floor((uploaded / total) * 100);
        if (percent >= lastLoggedPercent + 10) {
          lastLoggedPercent = percent;
          console.log(`  ${objectPath}: ${percent}%`);
        }
      },
      onError(error) {
        reject(new Error(`Upload failed for ${objectPath}: ${error.message}`));
      },
      onSuccess() {
        resolve();
      },
    });
    upload.start();
  });
}

async function uploadBuffer(objectPath, bytes, contentType) {
  if (DRY_RUN) {
    console.log(`[dry-run] upload ${objectPath} (${bytes.length} bytes)`);
    return `${SUPABASE_URL || "https://dry-run.local"}/storage/v1/object/public/${BUCKET}/${objectPath}`;
  }
  await uploadResumable(objectPath, bytes, contentType);
  const { data } = supabase.storage.from(BUCKET).getPublicUrl(objectPath);
  return data.publicUrl;
}

async function uploadLocalFile(objectPath, filePath, contentType) {
  if (!fs.existsSync(filePath)) {
    return null;
  }
  return uploadBuffer(objectPath, fs.readFileSync(filePath), contentType);
}

async function writeDoc(collection, id, values) {
  if (DRY_RUN) {
    console.log(`[dry-run] ${collection}/${id}`, values);
    return;
  }
  await firestore.collection(collection).doc(String(id)).set(values, { merge: true });
}

async function migrateBoCauHoi() {
  const data = rows("BoCauHoi");
  for (const row of data) {
    const id = asNumber(row.ID_Bo);
    await writeDoc("boCauHoi", id, {
      idBo: id,
      stt: asNumber(row.STT),
      tenBo: asText(row.TenBoCauHoi),
    });
  }
  return data.length;
}

async function migrateTracNghiem() {
  const data = rows("TracNghiem");
  for (const row of data) {
    const id = asNumber(row.ID_Cau);
    await writeDoc("tracNghiem", id, {
      idCau: id,
      idBo: asNumber(row.ID_Bo),
      noiDung: asText(row.NoiDung),
      dapAnA: asText(row.DapAn_A),
      dapAnB: asText(row.DapAn_B),
      dapAnC: asText(row.DapAn_C),
      dapAnD: asText(row.DapAn_D),
      dapAnTrue: asText(row.DapAn_True),
    });
  }
  return data.length;
}

async function migrateDienKhuyet() {
  const data = rows("DienKhuyet");
  for (const row of data) {
    const id = asNumber(row.ID_Cau);
    await writeDoc("dienKhuyet", id, {
      idCau: id,
      idBo: asNumber(row.ID_Bo),
      noiDung: asText(row.NoiDung),
      dapAn: asText(row.DapAn),
      goiY: asText(row.GoiY),
    });
  }
  return data.length;
}

async function migrateSapXepCau() {
  const data = rows("SapXepCau");
  for (const row of data) {
    const id = asNumber(row.IDCau);
    await writeDoc("sapXepCau", id, {
      idCau: id,
      idBo: asNumber(row.IDBo),
      dapAn: asText(row.DapAn),
      part1: asText(row.Part1),
      part2: asText(row.Part2),
      part3: asText(row.Part3),
      part4: asText(row.Part4),
    });
  }
  return data.length;
}

async function migrateLuyenNghe() {
  const data = rows("LuyenNghe");
  for (const row of data) {
    const id = asNumber(row.ID_Bai);
    const imageBytes = row.HinhAnh instanceof Uint8Array ? Buffer.from(row.HinhAnh) : null;
    let imageUrl = "";
    if (imageBytes) {
      const info = imageInfo(imageBytes);
      imageUrl = await uploadBuffer(`listening/images/${id}.${info.ext}`, imageBytes, info.contentType);
    }

    const audioBase = `hinh${String(id).padStart(2, "0")}`;
    const mp3Name = `${audioBase}.mp3`;
    const wavName = `${audioBase}.wav`;
    let audioUrl = await uploadLocalFile(
      `listening/audio/${mp3Name}`,
      path.join(NGHE_DIR, mp3Name),
      "audio/mpeg",
    );

    if (!audioUrl) {
      const wavPath = path.join(NGHE_DIR, wavName);
      if (fs.existsSync(wavPath)) {
        const wavBytes = fs.readFileSync(wavPath);
        console.log(`Converting ${wavName} (${wavBytes.length} bytes) to MP3`);
        const mp3Bytes = wavToMp3(wavBytes);
        console.log(`  ${mp3Name}: ${mp3Bytes.length} bytes`);
        audioUrl = await uploadBuffer(
          `listening/audio/${mp3Name}`,
          mp3Bytes,
          "audio/mpeg",
        );
      }
    }
    audioUrl = audioUrl ?? asText(row.Audio);

    await writeDoc("luyenNghe", id, {
      idBai: id,
      idBo: asNumber(row.ID_Bo),
      dapAnA: asText(row.DapAn_A),
      dapAnB: asText(row.DapAn_B),
      dapAnC: asText(row.DapAn_C),
      dapAnD: asText(row.DapAn_D),
      dapAnTrue: asText(row.DapAn_True),
      hinhAnhUrl: imageUrl,
      audio: audioUrl,
    });
  }
  return data.length;
}

async function migrateTuVung() {
  const data = rows("TuVung");
  let migrated = 0;
  for (const row of data) {
    const idBo = asNumber(row.ID_Bo);
    if (!INCLUDE_PLACEHOLDER_VOCAB && idBo !== 1) {
      console.log(`Skipping placeholder vocabulary row ${asNumber(row.ID_Tu)} (idBo=${idBo})`);
      continue;
    }

    const id = asNumber(row.ID_Tu);
    const imageBytes = row.HinhAnh instanceof Uint8Array ? Buffer.from(row.HinhAnh) : null;
    let imageUrl = "";
    if (imageBytes) {
      const info = imageInfo(imageBytes);
      imageUrl = await uploadBuffer(`vocabulary/images/${id}.${info.ext}`, imageBytes, info.contentType);
    }

    const dapAn = asText(row.DapAn);
    const audioName = `CSDL_TuVung_Bo${String(idBo).padStart(2, "0")}_${dapAn}.mp3`;
    const audioUrl = await uploadLocalFile(
      `vocabulary/audio/${audioName}`,
      path.join(TUVUNG_DIR, audioName),
      "audio/mpeg",
    ) ?? asText(row.Audio);

    await writeDoc("tuVung", id, {
      idTu: id,
      idBo,
      dapAn,
      dichNghia: asText(row.DichNghia),
      loaiTu: asText(row.LoaiTu),
      audio: audioUrl,
      anhUrl: imageUrl,
    });
    migrated += 1;
  }
  return migrated;
}

function maxId(table, column) {
  const data = rows(table);
  return data.reduce((max, row) => Math.max(max, asNumber(row[column])), 0);
}

async function migrateCounters() {
  const counters = {
    boCauHoi: maxId("BoCauHoi", "ID_Bo"),
    tracNghiem: maxId("TracNghiem", "ID_Cau"),
    dienKhuyet: maxId("DienKhuyet", "ID_Cau"),
    sapXepCau: maxId("SapXepCau", "IDCau"),
    luyenNghe: maxId("LuyenNghe", "ID_Bai"),
    tuVung: maxId("TuVung", "ID_Tu"),
  };
  if (DRY_RUN) {
    console.log("[dry-run] meta/counters", counters);
  } else {
    await firestore.collection("meta").doc("counters").set(counters, { merge: true });
  }
}

console.log(`Migrating ${DB_PATH}`);
const counts = {
  boCauHoi: await migrateBoCauHoi(),
  tracNghiem: await migrateTracNghiem(),
  dienKhuyet: await migrateDienKhuyet(),
  sapXepCau: await migrateSapXepCau(),
  luyenNghe: await migrateLuyenNghe(),
  tuVung: await migrateTuVung(),
};
await migrateCounters();

console.log("Migration complete", counts);
if (DRY_RUN) {
  console.log("Dry run only; nothing was written.");
}
